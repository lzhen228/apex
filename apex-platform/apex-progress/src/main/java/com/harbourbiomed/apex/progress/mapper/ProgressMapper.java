package com.harbourbiomed.apex.progress.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProgressMapper {

    List<Map<String, Object>> getTargetsByDisease(@Param("diseaseId") Integer diseaseId);

    List<Map<String, Object>> getDiseaseViewData(
            @Param("diseaseId") Integer diseaseId,
            @Param("targets") List<String> targets);

    String getDiseaseName(@Param("diseaseId") Integer diseaseId);

    String getLatestSyncTime();
}
