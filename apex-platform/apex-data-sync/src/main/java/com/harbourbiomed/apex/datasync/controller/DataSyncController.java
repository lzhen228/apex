package com.harbourbiomed.apex.datasync.controller;

import com.harbourbiomed.apex.common.result.Result;
import com.harbourbiomed.apex.datasync.service.DataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 数据同步手动触发接口（仅管理员可用）。
 * 用于运维人员在 Parquet 文件准备好后立即触发同步，
 * 无需等待每日凌晨定时任务。
 */
@Slf4j
@RestController
@RequestMapping("/v1/data-sync")
@RequiredArgsConstructor
public class DataSyncController {

    private final DataSyncService dataSyncService;
    private final AtomicBoolean manualRunning = new AtomicBoolean(false);

    /**
     * 手动触发数据同步。
     * 同一时间只允许一个手动同步任务运行，重复请求将返回 409。
     */
    @PostMapping("/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> trigger() {
        if (!manualRunning.compareAndSet(false, true)) {
            return Result.fail(4001, "同步任务已在执行中，请稍后再试");
        }
        String batchId = UUID.randomUUID().toString();
        // 异步执行，避免接口超时
        Thread.ofVirtual().name("data-sync-manual-" + batchId).start(() -> {
            try {
                log.info("[DataSync] 手动触发同步，批次={}", batchId);
                dataSyncService.sync(batchId);
            } catch (Exception e) {
                log.error("[DataSync] 手动同步失败，批次={}，原因：{}", batchId, e.getMessage(), e);
            } finally {
                manualRunning.set(false);
            }
        });
        return Result.ok("同步任务已提交，批次 ID=" + batchId);
    }
}
