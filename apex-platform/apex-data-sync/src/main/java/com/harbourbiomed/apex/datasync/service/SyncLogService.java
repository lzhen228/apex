package com.harbourbiomed.apex.datasync.service;

import com.harbourbiomed.apex.datasync.entity.DataSyncLog;
import com.harbourbiomed.apex.datasync.mapper.SyncLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 同步日志服务
 * 
 * 负责记录数据同步的执行日志
 * 
 * @author Harbour BioMed
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncLogService {

    private final SyncLogMapper syncLogMapper;

    /**
     * 记录同步开始
     * 
     * @param module 模块名称（如：ci_tracking）
     * @param batchId 批次 ID
     * @return 同步日志对象
     */
    public DataSyncLog startSync(String module, String batchId) {
        log.info("记录同步开始: module={}, batchId={}", module, batchId);
        
        DataSyncLog syncLog = new DataSyncLog();
        syncLog.setModule(module);
        syncLog.setSyncBatchId(batchId);
        syncLog.setStartTime(LocalDateTime.now());
        syncLog.setStatus("running");
        syncLog.setRecordCount(0L);
        syncLog.setCreatedAt(LocalDateTime.now());
        
        syncLogMapper.insert(syncLog);
        log.info("同步日志已记录，ID: {}", syncLog.getId());
        
        return syncLog;
    }

    /**
     * 记录同步结束
     * 
     * @param batchId 批次 ID
     * @param status 同步状态（success | failed）
     * @param recordCount 同步记录数量
     * @param errorMessage 错误信息（失败时）
     */
    public void endSync(String batchId, String status, Long recordCount, String errorMessage) {
        log.info("记录同步结束: batchId={}, status={}, recordCount={}", batchId, status, recordCount);
        
        DataSyncLog syncLog = syncLogMapper.getByBatchId(batchId);
        if (syncLog != null) {
            syncLog.setEndTime(LocalDateTime.now());
            syncLog.setStatus(status);
            syncLog.setRecordCount(recordCount);
            syncLog.setErrorMessage(errorMessage);
            
            syncLogMapper.updateById(syncLog);
            log.info("同步日志已更新");
        } else {
            log.warn("未找到批次 ID 为 {} 的同步日志", batchId);
        }
    }

    /**
     * 查询同步日志列表
     * 
     * @param module 模块名称
     * @param limit 返回数量限制
     * @return 同步日志列表
     */
    public List<DataSyncLog> getLatestSyncLogs(String module, int limit) {
        log.debug("查询同步日志: module={}, limit={}", module, limit);
        return syncLogMapper.getLatestSyncLogs(module, limit);
    }

    /**
     * 获取最新的同步日志
     * 
     * @param module 模块名称
     * @return 最新的同步日志
     */
    public DataSyncLog getLatestSyncLog(String module) {
        List<DataSyncLog> logs = getLatestSyncLogs(module, 1);
        return logs.isEmpty() ? null : logs.get(0);
    }
}
