package com.harbourbiomed.apex.competition.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CompetitionMapper {

    List<Map<String, Object>> getDiseaseTree();

    List<Map<String, Object>> queryMatrixData(
            @Param("diseaseIds") List<Integer> diseaseIds,
            @Param("phases") List<String> phases);

        List<Map<String, Object>> queryTargetSummary(
            @Param("diseaseIds") List<Integer> diseaseIds,
            @Param("phases") List<String> phases);

    List<Map<String, Object>> getCellDrugs(
            @Param("target") String target,
            @Param("pairTarget") String pairTarget,
            @Param("diseaseIds") List<Integer> diseaseIds,
            @Param("phases") List<String> phases);

    String getDiseaseName(@Param("diseaseId") Integer diseaseId);

    String getLatestSyncTime();

    /**
     * 导出用平铺明细查询：每行一条药品-疾病记录，含所有导出所需字段
     */
    List<Map<String, Object>> exportDrugPipeline(
            @Param("diseaseIds") List<Integer> diseaseIds,
            @Param("phases") List<String> phases);
}
