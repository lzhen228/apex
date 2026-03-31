package com.harbourbiomed.apex.competition.vo;

import lombok.Data;

import java.util.List;

@Data
public class CellDrugsResponse {
    private String target;
    private String pairTarget;
    private List<DrugVO> drugs;

    @Data
    public static class DrugVO {
        private String drugNameEn;
        private String originator;
        private String researchInstitute;
        private String highestPhase;
        private String highestPhaseDate;
        private String nctId;
    }
}
