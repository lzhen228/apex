package com.harbourbiomed.apex.datasync.controller;

import com.harbourbiomed.apex.common.result.Result;
import com.harbourbiomed.apex.datasync.entity.DataSyncLog;
import com.harbourbiomed.apex.datasync.service.DataSyncService;
import com.harbourbiomed.apex.datasync.service.SyncLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据同步控制器
 * 
 * 提供数据同步的 HTTP 接口
 * 
 * @author Harbour BioMed
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/data-sync")
@RequiredArgsConstructor
@Tag(name = "数据同步管理", description = "数据同步相关接口")
public class DataSyncController {

    private final DataSyncService dataSyncService;
    private final SyncLogService syncLogService;

    /**
     * 手动触发同步
     * 
     * @return 同步结果，包含批次 ID
     */
    @PostMapping("/trigger")
    @Operation(summary = "手动触发同步", description = "手动触发 CI 追踪信息数据同步")
    public Result<Map<String, String>> triggerSync() {
        log.info("收到手动触发同步请求");
        
        try {
            String batchId = dataSyncService.syncCiTrackingInfo();
            Map<String, String> result = Map.of("batchId", batchId);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("触发同步失败", e);
            return Result.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新同步状态
     * 
     * @return 最新同步批次信息
     */
    @GetMapping("/status")
    @Operation(summary = "获取最新同步状态", description = "查询最新的数据同步状态")
    public Result<DataSyncLog> getLatestSyncStatus() {
        log.info("查询最新同步状态");
        
        DataSyncLog syncLog = dataSyncService.getLatestSyncBatch();
        if (syncLog == null) {
            return Result.error("暂无同步记录");
        }
        
        return Result.success(syncLog);
    }

    /**
     * 查询同步日志列表
     * 
     * @param limit 返回数量限制，默认 10
     * @return 同步日志列表
     */
    @GetMapping("/logs")
    @Operation(summary = "查询同步日志列表", description = "查询数据同步的历史日志")
    public Result<List<DataSyncLog>> getSyncLogs(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("查询同步日志列表，limit={}", limit);
        
        // 限制最大返回数量
        if (limit > 100) {
            limit = 100;
        }
        
        List<DataSyncLog> logs = syncLogService.getLatestSyncLogs("ci_tracking", limit);
        return Result.success(logs);
    }
}
