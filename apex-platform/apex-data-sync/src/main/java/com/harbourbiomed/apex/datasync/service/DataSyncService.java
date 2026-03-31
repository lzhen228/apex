package com.harbourbiomed.apex.datasync.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.harbourbiomed.apex.datasync.config.OssProperties;
import com.harbourbiomed.apex.datasync.entity.CiTrackingInfoEntity;
import com.harbourbiomed.apex.datasync.entity.DataSyncLog;
import com.harbourbiomed.apex.datasync.mapper.CiTrackingInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 数据同步服务
 * 
 * 负责完整的 OSS -> Parquet -> PostgreSQL 数据同步流程
 * 
 * @author Harbour BioMed
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncService {

    private static final String REDIS_LOCK_KEY_PREFIX = "data_sync_lock:";
    private static final int LOCK_TIMEOUT_SECONDS = 3600; // 锁超时时间：1小时
    private static final int BATCH_SIZE = 1000; // 批量插入大小
    private static final int KEEP_BATCH_COUNT = 3; // 保留的最新批次数量

    private final OssService ossService;
    private final ParquetReaderService parquetReaderService;
    private final CiTrackingInfoMapper ciTrackingInfoMapper;
    private final SyncLogService syncLogService;
    private final OssProperties ossProperties;
    private final StringRedisTemplate redisTemplate;

    /**
     * 同步 CI 追踪信息
     * 
     * 完整同步流程：
     * 1. 从 OSS 下载最新 Parquet 文件
     * 2. 读取 Parquet 数据
     * 3. 生成 sync_batch_id（UUID）
     * 4. 批量插入/更新数据库
     * 5. 清理旧数据（保留最新 3 个 batch）
     * 6. 记录同步日志
     * 
     * @return 同步批次 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public String syncCiTrackingInfo() {
        String module = "ci_tracking";
        String lockKey = REDIS_LOCK_KEY_PREFIX + module;
        
        // 尝试获取分布式锁
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new RuntimeException("数据同步正在进行中，请稍后重试");
        }

        try {
            // 生成批次 ID
            String batchId = UUID.randomUUID().toString();
            String localPath = getLocalFilePath(batchId);
            log.info("开始同步 CI 追踪信息，批次 ID: {}", batchId);

            // 记录同步开始
            DataSyncLog syncLog = syncLogService.startSync(module, batchId);

            try {
                // 从 OSS 下载文件
                String ossPath = ossProperties.getCiTrackingParquetPath();
                ossService.downloadFile(ossPath, localPath);
                log.info("文件下载完成: {}", localPath);

                // 读取 Parquet 数据
                List<CiTrackingInfoEntity> entities = parquetReaderService.readCiTrackingInfo(localPath);
                log.info("读取到 {} 条记录", entities.size());

                // 设置批次 ID 和同步时间
                LocalDateTime syncedAt = LocalDateTime.now();
                for (CiTrackingInfoEntity entity : entities) {
                    entity.setSyncBatchId(batchId);
                    entity.setSyncedAt(syncedAt);
                }

                // 批量插入数据库
                batchInsert(entities);
                log.info("数据插入完成");

                // 清理旧批次数据
                cleanOldBatches(batchId);

                // 记录同步成功
                syncLogService.endSync(batchId, "success", (long) entities.size(), null);

                log.info("同步成功，批次 ID: {}, 同步记录数: {}", batchId, entities.size());
                return batchId;

            } catch (Exception e) {
                log.error("同步失败，批次 ID: {}", batchId, e);
                // 记录同步失败
                syncLogService.endSync(batchId, "failed", 0L, e.getMessage());
                throw new RuntimeException("同步失败: " + e.getMessage(), e);
            } finally {
                // 清理临时文件
                deleteLocalFile(localPath);
            }

        } finally {
            // 释放锁
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 批量插入数据
     * 
     * @param entities 实体列表
     */
    private void batchInsert(List<CiTrackingInfoEntity> entities) {
        int total = entities.size();
        int fromIndex = 0;

        while (fromIndex < total) {
            int toIndex = Math.min(fromIndex + BATCH_SIZE, total);
            List<CiTrackingInfoEntity> batch = entities.subList(fromIndex, toIndex);

            log.info("批量插入数据：{} - {} / {}", fromIndex + 1, toIndex, total);
            ciTrackingInfoMapper.batchInsert(batch);

            fromIndex = toIndex;
        }
    }

    /**
     * 清理旧批次数据（保留最新的 N 个批次）
     * 
     * @param currentBatchId 当前批次 ID
     */
    private void cleanOldBatches(String currentBatchId) {
        log.info("开始清理旧批次数据");

        List<String> batchIds = ciTrackingInfoMapper.getAllBatchIds();
        log.info("当前共有 {} 个批次", batchIds.size());

        if (batchIds.size() <= KEEP_BATCH_COUNT) {
            log.info("批次数量未超过保留限制，无需清理");
            return;
        }

        // 排除当前批次，保留最新的 KEEP_BATCH_COUNT 个批次
        List<String> batchesToDelete = new ArrayList<>();
        int keepCount = 0;
        for (String batchId : batchIds) {
            if (batchId.equals(currentBatchId)) {
                continue;
            }
            if (keepCount < KEEP_BATCH_COUNT) {
                keepCount++;
            } else {
                batchesToDelete.add(batchId);
            }
        }

        log.info("需要删除 {} 个旧批次", batchesToDelete.size());
        for (String batchId : batchesToDelete) {
            long count = ciTrackingInfoMapper.countByBatchId(batchId);
            int deleted = ciTrackingInfoMapper.deleteByBatchId(batchId);
            log.info("删除批次 {}，删除 {} 条记录", batchId, deleted);
        }

        log.info("旧批次数据清理完成");
    }

    /**
     * 获取最新同步批次信息
     * 
     * @return 最新的同步日志
     */
    public DataSyncLog getLatestSyncBatch() {
        return syncLogService.getLatestSyncLog("ci_tracking");
    }

    /**
     * 获取本地文件路径
     * 
     * @param batchId 批次 ID
     * @return 本地文件路径
     */
    private String getLocalFilePath(String batchId) {
        String fileName = "ci_tracking_" + batchId + ".parquet";
        return Paths.get(ossProperties.getLocalTempDir(), fileName).toString();
    }

    /**
     * 删除本地临时文件
     * 
     * @param filePath 文件路径
     */
    private void deleteLocalFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("本地临时文件已删除: {}", filePath);
            }
        } catch (Exception e) {
            log.warn("删除本地文件失败: {}", filePath, e);
        }
    }
}
