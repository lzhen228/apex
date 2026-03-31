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
}
