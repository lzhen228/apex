package com.harbourbiomed.apex.datasync.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CI 追踪信息实体
 * 
 * 存储从 Parquet 文件中读取的竞争情报数据
 * 
 * @author Harbour BioMed
 */
@Data
@TableName("ci_tracking_info")
@Schema(description = "CI 追踪信息实体")
public class CiTrackingInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableField("id")
    @Schema(description = "主键 ID")
    private Long id;

    /**
     * 药物 ID
     */
    @TableField("drug_id")
    @Schema(description = "药物 ID")
    private String drugId;

    /**
     * 药物英文名称
     */
    @TableField("drug_name_en")
    @Schema(description = "药物英文名称")
    private String drugNameEn;

    /**
     * 药物中文名称
     */
    @TableField("drug_name_cn")
    @Schema(description = "药物中文名称")
    private String drugNameCn;

    /**
     * 靶点列表（逗号分隔字符串）
     */
    @TableField("targets")
    @Schema(description = "靶点列表")
    private String[] targets;

    /**
     * 靶点原始字符串
     */
    @TableField("targets_raw")
    @Schema(description = "靶点原始字符串")
    private String targetsRaw;

    /**
     * 疾病 ID
     */
    @TableField("disease_id")
    @Schema(description = "疾病 ID")
    private String diseaseId;

    /**
     * 港口适应症名称
     */
    @TableField("harbour_indication_name")
    @Schema(description = "港口适应症名称")
    private String harbourIndicationName;

    /**
     * 治疗领域（Therapeutic Area）
     */
    @TableField("ta")
    @Schema(description = "治疗领域")
    private String ta;

    /**
     * 作用机制（Mechanism of Action）
     */
    @TableField("moa")
    @Schema(description = "作用机制")
    private String moa;

    /**
     * 原研机构
     */
    @TableField("originator")
    @Schema(description = "原研机构")
    private String originator;

    /**
     * 研究机构
     */
    @TableField("research_institute")
    @Schema(description = "研究机构")
    private String researchInstitute;

    /**
     * 全球最高研发阶段
     */
    @TableField("global_highest_phase")
    @Schema(description = "全球最高研发阶段")
    private String globalHighestPhase;

    /**
     * 全球最高研发阶段分值
     */
    @TableField("global_highest_phase_score")
    @Schema(description = "全球最高研发阶段分值")
    private Integer globalHighestPhaseScore;

    /**
     * 首要适应症全球最新阶段
     */
    @TableField("indication_top_global_latest_stage")
    @Schema(description = "首要适应症全球最新阶段")
    private String indicationTopGlobalLatestStage;

    /**
     * 首要适应症全球开始日期
     */
    @TableField("indication_top_global_start_date")
    @Schema(description = "首要适应症全球开始日期")
    private String indicationTopGlobalStartDate;

    /**
     * 最高试验 ID
     */
    @TableField("highest_trial_id")
    @Schema(description = "最高试验 ID")
    private String highestTrialId;

    /**
     * 最高试验阶段
     */
    @TableField("highest_trial_phase")
    @Schema(description = "最高试验阶段")
    private String highestTrialPhase;

    /**
     * NCT 试验编号
     */
    @TableField("nct_id")
    @Schema(description = "NCT 试验编号")
    private String nctId;

    /**
     * 数据来源
     */
    @TableField("data_source")
    @Schema(description = "数据来源")
    private String dataSource;

    /**
     * 同步批次 ID
     */
    @TableField("sync_batch_id")
    @Schema(description = "同步批次 ID")
    private String syncBatchId;

    /**
     * 同步时间
     */
    @TableField("synced_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "同步时间")
    private LocalDateTime syncedAt;

    /**
     * 创建时间
     */
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
