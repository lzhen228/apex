package com.harbourbiomed.apex.progress.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 药物卡片值对象
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "药物卡片值对象")
public class DrugCardVO {

    /**
     * 药物 ID
     */
    @Schema(description = "药物 ID")
    private String drugId;

    /**
     * 药物英文名称
     */
    @Schema(description = "药物英文名称")
    private String drugNameEn;

    /**
     * 药物中文名称
     */
    @Schema(description = "药物中文名称")
    private String drugNameCn;

    /**
     * 全球最高研发阶段
     */
    @Schema(description = "全球最高研发阶段")
    private String globalHighestPhase;

    /**
     * 阶段分值
     */
    @Schema(description = "阶段分值")
    private Double phaseScore;

    /**
     * 起源公司
     */
    @Schema(description = "起源公司")
    private String originator;

    /**
     * 作用机制
     */
    @Schema(description = "作用机制")
    private String moa;

    /**
     * NCT 试验编号
     */
    @Schema(description = "NCT 试验编号")
    private String nctId;

    /**
     * 首要适应症最高阶段
     */
    @Schema(description = "首要适应症最高阶段")
    private String indicationTopStage;

    /**
     * 首要适应症开始日期
     */
    @Schema(description = "首要适应症开始日期")
    private Date indicationStartDate;

    /**
     * 首要适应症结束日期
     */
    @Schema(description = "首要适应症结束日期")
    private Date indicationEndDate;

    /**
     * 靶点列表
     */
    @Schema(description = "靶点列表")
    private List<String> targets;

    /**
     * 研究机构
     */
    @Schema(description = "研究机构")
    private String researchInstitute;
}
