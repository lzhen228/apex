package com.harbourbiomed.apex.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.harbourbiomed.apex.competition.entity.CiTrackingInfo;
import com.harbourbiomed.apex.competition.request.MatrixQueryRequest;
import com.harbourbiomed.apex.competition.vo.CellDrugVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 竞争情报信息 Mapper
 *
 * @author Harbour BioMed
 */
public interface CompetitionCiTrackingInfoMapper extends BaseMapper<CiTrackingInfo> {

    /**
     * 查询矩阵数据
     *
     * @param request 查询请求
     * @return 竞争情报列表
     */
    List<CiTrackingInfo> queryMatrixData(@Param("request") MatrixQueryRequest request);

    /**
     * 查询指定靶点和疾病的药物详情
     *
     * @param target 靶点名称
     * @param diseaseId 疾病 ID
     * @param phases 研发阶段列表
     * @return 药物详情列表
     */
    List<CellDrugVO> queryCellDrugs(@Param("target") String target,
                                     @Param("diseaseId") Integer diseaseId,
                                     @Param("phasesList") List<String> phases);

    /**
     * 查询靶点聚合信息
     *
     * @param request 查询请求
     * @return 靶点聚合信息列表
     */
    List<Map<String, Object>> queryTargetAggregation(@Param("request") MatrixQueryRequest request);
}
