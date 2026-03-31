package com.harbourbiomed.apex.progress.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 疾病进展视图查询记录
 */
@Data
public class ProgressDrugRecord {

    private String drugId;
    private String drugNameEn;
    private String drugNameCn;
    private String[] targets;
    private Integer diseaseId;
    private Integer taId;
    private String diseaseNameEn;
    private String diseaseNameCn;
    private String moa;
    private String originator;
    private String researchInstitute;
    private String globalHighestPhase;
    private BigDecimal globalHighestPhaseScore;
    private String indicationTopGlobalLatestStage;
    private LocalDate indicationTopGlobalStartDate;
    private String highestTrialId;
    private String highestTrialPhase;
    private String nctId;
    private String dataSource;
}
