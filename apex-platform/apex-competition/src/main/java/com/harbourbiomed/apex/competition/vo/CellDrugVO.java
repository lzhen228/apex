package com.harbourbiomed.apex.competition.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 矩阵单元格药物详情视图对象
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "矩阵单元格药物详情视图对象")
public class CellDrugVO {

    @Schema(description = "药物 ID")
    private String drugId;

    @Schema(description = "药物英文名称")
    private String drugNameEn;

    @Schema(description = "药物中文名称")
    private String drugNameCn;

    @Schema(description = "研发阶段")
    private String phase;

    @Schema(description = "阶段分值")
    private Double phaseScore;

    @Schema(description = "原研机构")
    private String originator;

    @Schema(description = "作用机制")
    private String moa;

    @Schema(description = "NCT 编号")
    private String nctId;

    @Schema(description = "药物涉及的所有靶点")
    private List<String> targets;

    @Schema(description = "适应症开始时间")
    private LocalDate indicationStartDate;
}
