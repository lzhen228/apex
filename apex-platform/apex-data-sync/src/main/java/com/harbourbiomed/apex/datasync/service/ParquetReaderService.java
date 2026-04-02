package com.harbourbiomed.apex.datasync.service;

import com.harbourbiomed.apex.datasync.dto.CiTrackingRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Parquet 文件读取服务
 * <p>
 * 使用 DuckDB JDBC（内存模式）直接查询本地 Parquet 文件，将所有行映射为
 * {@link CiTrackingRecord}。此处仅做字段提取和基础类型转换，
 * 阶段归一化由 {@link PhaseNormalizationService} 完成。
 * <p>
 * 安全说明：Parquet 文件路径来自应用配置（非用户输入），
 * 使用 PreparedStatement 参数化传入以防止 JDBC 层注入。
 */
@Slf4j
@Service
public class ParquetReaderService {

    /**
     * 读取指定路径的 Parquet 文件并返回记录列表。
     *
     * @param parquetFilePath 服务器本地绝对路径
     * @return 原始 {@link CiTrackingRecord} 列表（阶段字段尚未归一化）
     */
    public List<CiTrackingRecord> read(String parquetFilePath) throws Exception {
        log.info("[DataSync] 开始读取 Parquet 文件: {}", parquetFilePath);

        // 列名常量，与 pharmcube2harbour_ci_tracking_info_0.parquet Schema 对应
        String sql = """
                SELECT
                    ta,
                    harbour_indication_name,
                    drug_id,
                    drug_name_en,
                    drug_name_cn,
                    targets,
                    moa,
                    originator,
                    research_institute,
                    global_highest_phase,
                    highest_trial_id,
                    highest_trial_phase,
                    nct_id,
                    data_source,
                    indication_top_global_latest_stage,
                    indication_top_global_start_date
                FROM read_parquet(?)
                """;

        List<CiTrackingRecord> records = new ArrayList<>();

        // DuckDB 内存实例，仅用于 Parquet 解析，不持久化
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:");
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, parquetFilePath);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(mapRow(rs));
                }
            }
        }

        log.info("[DataSync] Parquet 读取完成，共 {} 条记录", records.size());
        return records;
    }

    // ─── 行映射 ───────────────────────────────────────────────────────

    private CiTrackingRecord mapRow(ResultSet rs) throws SQLException {
        String targetsRaw = getString(rs, "targets");
        String[] targets  = splitTargets(targetsRaw);

        return CiTrackingRecord.builder()
                .ta(getString(rs, "ta"))
                .harbourIndicationName(getString(rs, "harbour_indication_name"))
                .drugId(getString(rs, "drug_id"))
                .drugNameEn(getString(rs, "drug_name_en"))
                .drugNameCn(getString(rs, "drug_name_cn"))
                .targetsRaw(targetsRaw)
                .targets(targets)
                .moa(getString(rs, "moa"))
                .originator(getString(rs, "originator"))
                .researchInstitute(getString(rs, "research_institute"))
                .globalHighestPhase(getString(rs, "global_highest_phase"))
                .highestTrialId(getString(rs, "highest_trial_id"))
                .highestTrialPhase(getString(rs, "highest_trial_phase"))
                .nctId(getString(rs, "nct_id"))
                .dataSource(getString(rs, "data_source"))
                .indicationTopGlobalLatestStage(getString(rs, "indication_top_global_latest_stage"))
                .indicationTopGlobalStartDate(toLocalDate(rs, "indication_top_global_start_date"))
                // diseaseId / globalHighestPhaseScore 由 DataSyncService 填充
                .build();
    }

    private String getString(ResultSet rs, String col) throws SQLException {
        String v = rs.getString(col);
        return (v != null && !v.isBlank()) ? v.trim() : null;
    }

    private LocalDate toLocalDate(ResultSet rs, String col) throws SQLException {
        Object val = rs.getObject(col);
        if (val == null) return null;
        if (val instanceof LocalDate ld) return ld;
        // DuckDB 可能返回 java.sql.Date 或字符串
        if (val instanceof java.sql.Date d) return d.toLocalDate();
        if (val instanceof String s && !s.isBlank()) {
            try {
                return LocalDate.parse(s.trim().substring(0, 10));
            } catch (Exception e) {
                log.warn("[DataSync] 日期解析失败，列={}, 值={}", col, s);
                return null;
            }
        }
        return null;
    }

    /**
     * 将逗号分隔的靶点字符串拆分为数组，并去除首尾空白。
     * 示例："IL-17A, TNF" → ["IL-17A", "TNF"]
     */
    private String[] splitTargets(String raw) {
        if (raw == null || raw.isBlank()) return new String[0];
        String[] parts = raw.split(",");
        List<String> result = new ArrayList<>(parts.length);
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result.toArray(new String[0]);
    }
}
