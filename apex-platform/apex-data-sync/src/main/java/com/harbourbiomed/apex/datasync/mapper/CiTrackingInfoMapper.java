package com.harbourbiomed.apex.datasync.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.harbourbiomed.apex.datasync.entity.CiTrackingInfoEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * CI 追踪信息 Mapper
 * 
 * @author Harbour BioMed
 */
public interface CiTrackingInfoMapper extends BaseMapper<CiTrackingInfoEntity> {

    /**
     * 批量插入 CI 追踪信息
     * 
     * @param entities 实体列表
     * @return 插入行数
     */
    int batchInsert(@Param("entities") List<CiTrackingInfoEntity> entities);

    /**
     * 查询所有同步批次 ID（按创建时间倒序）
     * 
     * @return 批次 ID 列表
     */
    @Select("SELECT DISTINCT sync_batch_id FROM ci_tracking_info WHERE sync_batch_id IS NOT NULL ORDER BY created_at DESC")
    List<String> getAllBatchIds();

    /**
     * 删除指定批次的数据
     * 
     * @param batchId 批次 ID
     * @return 删除行数
     */
    int deleteByBatchId(@Param("batchId") String batchId);

    /**
     * 根据批次 ID 查询记录数量
     * 
     * @param batchId 批次 ID
     * @return 记录数量
     */
    @Select("SELECT COUNT(*) FROM ci_tracking_info WHERE sync_batch_id = #{batchId}")
    long countByBatchId(@Param("batchId") String batchId);
}
