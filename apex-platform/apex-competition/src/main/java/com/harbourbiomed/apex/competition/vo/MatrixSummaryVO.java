package com.harbourbiomed.apex.competition.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 矩阵汇总信息视图对象
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "矩阵汇总信息视图对象")
public class MatrixSummaryVO {

    @Schema(description = "总药物数")
    private Integer totalDrugs;

    @Schema(description = "总靶点数")
    private Integer totalTargets;

    @Schema(description = "总疾病数")
    private Integer totalDiseases;
}
