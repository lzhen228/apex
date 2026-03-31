package com.harbourbiomed.apex.datasync.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.harbourbiomed.apex.datasync.entity.DataSyncLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据同步日志 Mapper
 * 
 * @author Harbour BioMed
 */
public interface SyncLogMapper extends BaseMapper<DataSyncLog> {

    /**
     * 根据模块查询最新的同步日志
     * 
     * @param module 模块名称
     * @param limit 返回数量限制
     * @return 同步日志列表
     */
    List<DataSyncLog> getLatestSyncLogs(@Param("module") String module, @Param("limit") int limit);

    /**
     * 根据批次 ID 查询日志
     * 
     * @param batchId 批次 ID
     * @return 同步日志
     */
    DataSyncLog getByBatchId(@Param("batchId") String batchId);
}
