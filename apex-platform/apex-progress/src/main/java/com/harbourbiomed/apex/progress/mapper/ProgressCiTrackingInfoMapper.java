package com.harbourbiomed.apex.progress.mapper;

import com.harbourbiomed.apex.progress.dto.DiseaseViewRequest;
import com.harbourbiomed.apex.progress.dto.ProgressDrugRecord;
import com.harbourbiomed.apex.progress.dto.TargetStatVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * CI Tracking Info 进展查询 Mapper
 *
 * @author Harbour BioMed
 */
public interface ProgressCiTrackingInfoMapper {

    /**
     * 根据疾病 ID 查询靶点统计列表
     *
     * @param diseaseId 疾病 ID
     * @return 靶点统计列表
     */
    List<TargetStatVO> selectTargetsByDisease(@Param("diseaseId") Integer diseaseId);

    /**
     * 根据请求条件查询药物管线数据
     *
     * @param diseaseId 疾病 ID
     * @param targets   链点列表（可选）
     * @param phases    研发阶段列表（可选）
     * @param origins   起源公司列表（可选）
     * @param sortBy    排序字段
     * @param sortOrder 排序方向
     * @return 药物管线数据
     */
    List<ProgressDrugRecord> selectDiseaseView(
            @Param("diseaseId") Integer diseaseId,
            @Param("targets") List<String> targets,
            @Param("phases") List<String> phases,
            @Param("origins") List<String> origins,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder
    );
}
