package com.harbourbiomed.apex.competition.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 竞争情报信息实体
 *
 * @author Harbour BioMed
 */
@Data
@TableName("ci_tracking_latest")
@Schema(description = "竞争情报信息实体")
public class CiTrackingInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    @Schema(description = "主键 ID")
    private Long id;

    @TableField("drug_id")
    @Schema(description = "药品 ID")
    private String drugId;

    @TableField("drug_name_en")
    @Schema(description = "药品英文名称")
    private String drugNameEn;

    @TableField("drug_name_cn")
    @Schema(description = "药品中文名称")
    private String drugNameCn;

    @TableField("targets")
    @Schema(description = "靶点数组")
    private String[] targets;

    @TableField("targets_raw")
    @Schema(description = "靶点原始字符串")
    private String targetsRaw;

    @TableField("disease_id")
    @Schema(description = "疾病 ID")
    private Integer diseaseId;

    @TableField("harbour_indication_name")
    @Schema(description = "疾病名称")
    private String harbourIndicationName;

    @TableField("ta")
    @Schema(description = "治疗领域")
    private String ta;

    @TableField("moa")
    @Schema(description = "作用机制")
    private String moa;

    @TableField("originator")
    @Schema(description = "原研机构")
    private String originator;

    @TableField("research_institute")
    @Schema(description = "研究机构")
    private String researchInstitute;

    @TableField("global_highest_phase")
    @Schema(description = "全球最高研发阶段")
    private String globalHighestPhase;

    @TableField("global_highest_phase_score")
    @Schema(description = "全球最高研发阶段分值")
    private BigDecimal globalHighestPhaseScore;

    @TableField("indication_top_global_latest_stage")
    @Schema(description = "疾病全球最高阶段")
    private String indicationTopGlobalLatestStage;

    @TableField("indication_top_global_start_date")
    @Schema(description = "最高阶段开始时间")
    private LocalDate indicationTopGlobalStartDate;

    @TableField("highest_trial_id")
    @Schema(description = "最高临床试验 ID")
    private String highestTrialId;

    @TableField("highest_trial_phase")
    @Schema(description = "最高临床试验阶段")
    private String highestTrialPhase;

    @TableField("nct_id")
    @Schema(description = "NCT 编号")
    private String nctId;

    @TableField("data_source")
    @Schema(description = "数据来源")
    private String dataSource;

    @TableField("sync_batch_id")
    @Schema(description = "同步批次 ID")
    private String syncBatchId;

    @TableField("synced_at")
    @Schema(description = "同步时间")
    private java.time.LocalDateTime syncedAt;

    @TableField("created_at")
    @Schema(description = "创建时间")
    private java.time.LocalDateTime createdAt;
}
