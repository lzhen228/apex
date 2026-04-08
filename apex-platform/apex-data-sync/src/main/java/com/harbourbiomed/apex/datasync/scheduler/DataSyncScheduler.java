package com.harbourbiomed.apex.datasync.scheduler;

import com.harbourbiomed.apex.datasync.service.DataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 数据同步定时调度器
 * <p>
 * 每天凌晨 05:00 自动触发全量同步，将服务器 /home 目录下的
 * pharmcube2harbour_ci_tracking_info_0.parquet 写入 PostgreSQL。
 * <p>
 * 使用 {@link AtomicBoolean} 防止因上一次任务未完成时（如文件极大导致超时）
 * 重叠触发新任务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSyncScheduler {

    private final DataSyncService dataSyncService;

    /** 并发保护：防止同一时间重复执行 */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 定时任务：每天 05:00 执行（服务器本地时区）。
     * Cron 表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 5 * * *")
    public void scheduledSync() {
        if (!running.compareAndSet(false, true)) {
            log.warn("[DataSync] 上一次同步任务仍在执行，本次跳过");
            return;
        }
        String batchId = UUID.randomUUID().toString();
        log.info("[DataSync] 定时任务触发，批次={}", batchId);
        try {
            dataSyncService.sync(batchId);
        } catch (Throwable t) {
            log.error("[DataSync] 定时同步失败，批次={}，原因：{}", batchId, t.getMessage(), t);
        } finally {
            running.set(false);
        }
    }
}
