package com.harbourbiomed.apex.datasync.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ETL 管道内部 DTO — Parquet 原始数据经过清洗和阶段归一化后的中间记录，
 * 随后批量写入 PostgreSQL ci_tracking_latest 表。
 */
@Data
@Builder
public class CiTrackingRecord {

    // ─── 药品维度 ──────────────────────────────────────────────────
    private String drugId;
    private String drugNameEn;
    private String drugNameCn;

    /** 靶点数组（从逗号分隔的原始字符串拆分而来），写入 PostgreSQL TEXT[] */
    private String[] targets;
    /** 靶点原始字符串，如 "IL-17A,TNF" */
    private String targetsRaw;

    // ─── 疾病 / 治疗领域维度 ──────────────────────────────────────
    /** 对应 disease 维度表的主键，由 DataSyncService 在维度同步后填充 */
    private Integer diseaseId;
    private String harbourIndicationName;
    private String ta;

    // ─── 研发信息 ──────────────────────────────────────────────────
    private String moa;
    private String originator;
    private String researchInstitute;

    /** 药物层面的全球最高阶段（原始值，未归一化） */
    private String globalHighestPhase;

    /**
     * 疾病层面全球最高阶段（归一化后标准名称，如 "Phase III"）。
     * 源字段：indication_top_global_latest_stage，经 PhaseNormalizationService 处理。
     */
    private String indicationTopGlobalLatestStage;

    /** 疾病层面最高阶段分值（由归一化后阶段名计算，如 Phase III = 3.0） */
    private BigDecimal globalHighestPhaseScore;

    private LocalDate indicationTopGlobalStartDate;
    private String highestTrialId;
    private String highestTrialPhase;
    private String nctId;
    private String dataSource;

    // ─── 同步元数据 ────────────────────────────────────────────────
    private String syncBatchId;
    private LocalDateTime syncedAt;
}
