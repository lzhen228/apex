package com.harbourbiomed.apex.datasync.job;

import com.harbourbiomed.apex.datasync.entity.DataSyncLog;
import com.harbourbiomed.apex.datasync.service.DataSyncService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 数据同步定时任务
 * 
 * 使用 XXL-Job 定时执行数据同步任务
 * 
 * @author Harbour BioMed
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSyncJob {

    private final DataSyncService dataSyncService;

    /**
     * CI 追踪信息同步任务
     * 
     * 任务名称：ciTrackingInfoSyncJob
     * 执行流程：
     * 1. 记录任务开始日志
     * 2. 调用数据同步服务
     * 3. 记录任务完成日志
     * 4. 处理异常情况
     */
    @XxlJob("ciTrackingInfoSyncJob")
    public void syncCiTrackingInfo() {
        String jobParam = XxlJobHelper.getJobParam();
        log.info("XXL-Job 任务开始执行，任务参数: {}", jobParam);

        try {
            // 记录任务开始
            XxlJobHelper.log("开始同步 CI 追踪信息");
            
            // 执行数据同步
            String batchId = dataSyncService.syncCiTrackingInfo();
            
            // 记录任务成功
            String logMsg = String.format("同步成功，批次 ID: %s", batchId);
            log.info(logMsg);
            XxlJobHelper.log(logMsg);
            XxlJobHelper.handleSuccess();
            
        } catch (Exception e) {
            // 记录任务失败
            String errorMsg = String.format("同步失败: %s", e.getMessage());
            log.error(errorMsg, e);
            XxlJobHelper.log(errorMsg);
            XxlJobHelper.handleFail(errorMsg);
        }
    }
}
