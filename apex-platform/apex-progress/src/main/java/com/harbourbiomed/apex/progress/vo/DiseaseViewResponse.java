package com.harbourbiomed.apex.progress.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DiseaseViewResponse {
    private String diseaseName;
    private List<String> phases;
    private List<TargetRowVO> targetRows;
    private int totalDrugs;
    private String updatedAt;

    @Data
    public static class TargetRowVO {
        private String target;
        private Map<String, List<DrugVO>> phaseDrugs;
    }

    @Data
    public static class DrugVO {
        private String drugNameEn;
        private String originator;
        private String researchInstitute;
        private String highestPhaseDate;
        private String nctId;
    }
}
