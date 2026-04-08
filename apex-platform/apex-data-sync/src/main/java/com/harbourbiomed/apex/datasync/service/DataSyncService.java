package com.harbourbiomed.apex.datasync.service;

import com.harbourbiomed.apex.datasync.config.DataSyncProperties;
import com.harbourbiomed.apex.datasync.dto.CiTrackingRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.PreDestroy;

import java.sql.Array;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 数据同步核心服务：OSS Parquet → PostgreSQL 全量覆盖 ETL。
 *
 * <p><b>执行流程</b>：
 * <ol>
 *   <li>读取本地 Parquet 文件（DuckDB 解析）</li>
 *   <li>归一化研发阶段名称并计算分值</li>
 *   <li>同步治疗领域（therapeutic_area）和疾病（disease）维度表</li>
 *   <li>在单个事务内 TRUNCATE + 批量 INSERT ci_tracking_latest</li>
 * </ol>
 *
 * <p><b>全量覆盖策略</b>：TRUNCATE 后重新写入，整个过程在事务内完成，
 * 写入失败时自动回滚，在线查询仍读取上一次成功的数据。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncService {

    private static final String MODULE_NAME = "ci_tracking";
    private static final String INTERRUPTED_MESSAGE = "process interrupted or killed before completion";

    private final ParquetReaderService parquetReaderService;
    private final PhaseNormalizationService phaseNormalization;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final DataSyncProperties properties;
    private final AtomicReference<String> currentBatchId = new AtomicReference<>();

    private static final String INSERT_SQL = """
            INSERT INTO ci_tracking_latest (
                drug_id, drug_name_en, drug_name_cn,
                targets, targets_raw,
                disease_id, harbour_indication_name, ta,
                moa, originator, research_institute,
                global_highest_phase, global_highest_phase_score,
                indication_top_global_latest_stage, indication_top_global_start_date,
                highest_trial_id, highest_trial_phase,
                nct_id, data_source,
                sync_batch_id, synced_at, created_at
            ) VALUES (
                ?, ?, ?,
                ?, ?,
                ?, ?, ?,
                ?, ?, ?,
                ?, ?,
                ?, ?,
                ?, ?,
                ?, ?,
                ?, ?, NOW()
            )
            """;

    // ─── 公开入口 ──────────────────────────────────────────────────

    /**
     * 执行完整的数据同步流程。
     *
     * @param batchId 本次同步批次 ID（通常为 UUID），记录在每行数据中便于追溯
     */
    public void sync(String batchId) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("[DataSync] 开始同步，批次 ID={}", batchId);

        markInterruptedRuns(INTERRUPTED_MESSAGE);
        currentBatchId.set(batchId);

        // 记录 running 状态
        jdbcTemplate.update(
                "INSERT INTO data_sync_log (module, sync_batch_id, start_time, status) VALUES (?, ?, ?, 'running')",
            MODULE_NAME, batchId, Timestamp.valueOf(startTime));

        try {
            // Step 1：读取 Parquet
            List<CiTrackingRecord> records = parquetReaderService.read(properties.getParquetFilePath());
            if (records.isEmpty()) {
                log.warn("[DataSync] Parquet 文件无有效数据，同步终止");
                updateLog(batchId, startTime, "failed", 0, "Parquet 文件无有效数据");
                return;
            }

            // Step 2：归一化阶段 + 元数据
            LocalDateTime syncedAt = LocalDateTime.now();
            for (CiTrackingRecord r : records) {
                String std = phaseNormalization.normalize(r.getIndicationTopGlobalLatestStage());
                r.setIndicationTopGlobalLatestStage(std);
                r.setGlobalHighestPhaseScore(phaseNormalization.getScore(std));
                r.setSyncBatchId(batchId);
                r.setSyncedAt(syncedAt);
            }

            // Step 3-4：单事务内全量覆盖（含维度表）
            transactionTemplate.executeWithoutResult(status -> {

                log.info("[DataSync] TRUNCATE ci_tracking_latest");
                jdbcTemplate.execute("TRUNCATE TABLE ci_tracking_latest");
                log.info("[DataSync] TRUNCATE therapeutic_area CASCADE（含 disease）");
                jdbcTemplate.execute("TRUNCATE TABLE therapeutic_area CASCADE");

                Map<String, Set<String>> taToDeseases = new LinkedHashMap<>();
                for (CiTrackingRecord r : records) {
                    if (r.getTa() == null || r.getHarbourIndicationName() == null) continue;
                    taToDeseases.computeIfAbsent(r.getTa(), k -> new LinkedHashSet<>())
                            .add(r.getHarbourIndicationName());
                }

                for (String ta : taToDeseases.keySet()) {
                    jdbcTemplate.update("INSERT INTO therapeutic_area (name_en) VALUES (?)", ta);
                }

                Map<String, Integer> taIdMap = new HashMap<>();
                jdbcTemplate.query("SELECT name_en, id FROM therapeutic_area",
                        (RowCallbackHandler) rs -> taIdMap.put(rs.getString("name_en"), rs.getInt("id")));

                for (Map.Entry<String, Set<String>> entry : taToDeseases.entrySet()) {
                    Integer taId = taIdMap.get(entry.getKey());
                    if (taId == null) continue;
                    for (String diseaseName : entry.getValue()) {
                        jdbcTemplate.update("INSERT INTO disease (ta_id, name_en) VALUES (?, ?)", taId, diseaseName);
                    }
                }

                Map<String, Integer> diseaseIdMap = new HashMap<>();
                jdbcTemplate.query(
                        """
                        SELECT d.name_en, t.name_en AS ta_name, d.id
                        FROM disease d
                        JOIN therapeutic_area t ON t.id = d.ta_id
                        """,
                        (RowCallbackHandler) rs -> diseaseIdMap.put(
                                buildDiseaseKey(rs.getString("ta_name"), rs.getString("name_en")),
                                rs.getInt("id")));

                for (CiTrackingRecord r : records) {
                    r.setDiseaseId(diseaseIdMap.get(buildDiseaseKey(r.getTa(), r.getHarbourIndicationName())));
                }

                int batchSize = properties.getBatchSize();
                int total = records.size();
                for (int from = 0; from < total; from += batchSize) {
                    int to = Math.min(from + batchSize, total);
                    insertBatch(records.subList(from, to));
                    log.debug("[DataSync] 已写入 {}/{}", to, total);
                }
            });

            log.info("[DataSync] 同步完成，共写入 {} 条记录，批次={}", records.size(), batchId);
            updateLog(batchId, startTime, "success", records.size(), null);

        } catch (Throwable t) {
            log.error("[DataSync] 同步失败，批次={}", batchId, t);
            updateLog(batchId, startTime, "failed", 0, buildErrorMessage(t));
            if (t instanceof Exception e) {
                throw e;
            }
            throw new RuntimeException(t);
        } finally {
            currentBatchId.compareAndSet(batchId, null);
        }
    }

    @PreDestroy
    public void onShutdown() {
        String batchId = currentBatchId.getAndSet(null);
        if (batchId == null) {
            return;
        }

        log.warn("[DataSync] 服务关闭，批次 {} 未完成，尝试回写 failed 状态", batchId);
        try {
            markRunFailed(batchId, "application shutdown before sync completion");
        } catch (Exception e) {
            log.error("[DataSync] 服务关闭时回写同步状态失败，批次={}", batchId, e);
        }
    }

    private void updateLog(String batchId, LocalDateTime startTime, String status, int recordCount, String errorMsg) {
        jdbcTemplate.update(
                "UPDATE data_sync_log SET end_time=?, status=?, record_count=?, error_message=? WHERE sync_batch_id=?",
                Timestamp.valueOf(LocalDateTime.now()), status, (long) recordCount, errorMsg, batchId);
    }

    private void markInterruptedRuns(String message) {
        int affected = jdbcTemplate.update(
                """
                UPDATE data_sync_log
                SET end_time = COALESCE(end_time, NOW()),
                    status = 'failed',
                    error_message = COALESCE(error_message, ?)
                WHERE module = ? AND status = 'running'
                """,
                message, MODULE_NAME);

        if (affected > 0) {
            log.warn("[DataSync] 检测到 {} 条历史 running 记录，已标记为 failed", affected);
        }
    }

    private void markRunFailed(String batchId, String message) {
        jdbcTemplate.update(
                """
                UPDATE data_sync_log
                SET end_time = COALESCE(end_time, NOW()),
                    status = 'failed',
                    error_message = COALESCE(error_message, ?)
                WHERE module = ? AND sync_batch_id = ? AND status = 'running'
                """,
                message, MODULE_NAME, batchId);
    }

    private String buildErrorMessage(Throwable t) {
        String message = t.getMessage();
        if (message == null || message.isBlank()) {
            return t.getClass().getSimpleName();
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private String buildDiseaseKey(String ta, String diseaseName) {
        return ta + "::" + diseaseName;
    }

    private void insertBatch(List<CiTrackingRecord> batch) {
        jdbcTemplate.batchUpdate(INSERT_SQL, batch, batch.size(), (ps, r) -> {
            ps.setString(1, r.getDrugId());
            ps.setString(2, r.getDrugNameEn());
            ps.setString(3, r.getDrugNameCn());

            // TEXT[] — 必须通过 JDBC Connection.createArrayOf 构造
            Array targetsArray = ps.getConnection().createArrayOf("text",
                    r.getTargets() != null ? r.getTargets() : new String[0]);
            ps.setArray(4, targetsArray);
            ps.setString(5, r.getTargetsRaw());

            if (r.getDiseaseId() != null) {
                ps.setInt(6, r.getDiseaseId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.setString(7, r.getHarbourIndicationName());
            ps.setString(8, r.getTa());
            ps.setString(9, r.getMoa());
            ps.setString(10, r.getOriginator());
            ps.setString(11, r.getResearchInstitute());
            ps.setString(12, r.getGlobalHighestPhase());

            if (r.getGlobalHighestPhaseScore() != null) {
                ps.setBigDecimal(13, r.getGlobalHighestPhaseScore());
            } else {
                ps.setNull(13, java.sql.Types.NUMERIC);
            }

            ps.setString(14, r.getIndicationTopGlobalLatestStage());

            if (r.getIndicationTopGlobalStartDate() != null) {
                ps.setDate(15, Date.valueOf(r.getIndicationTopGlobalStartDate()));
            } else {
                ps.setNull(15, java.sql.Types.DATE);
            }

            ps.setString(16, r.getHighestTrialId());
            ps.setString(17, r.getHighestTrialPhase());
            ps.setString(18, r.getNctId());
            ps.setString(19, r.getDataSource());
            ps.setString(20, r.getSyncBatchId());
            ps.setTimestamp(21, Timestamp.valueOf(r.getSyncedAt()));
        });
    }
}
