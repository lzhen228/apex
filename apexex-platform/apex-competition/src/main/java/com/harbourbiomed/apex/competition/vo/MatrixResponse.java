package com.harbourbiomed.apex.competition.vo;

import io.swagger.swagger3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 矩阵响应对象
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "竞争格局矩阵响应")
public class MatrixResponse {

    @Schema(description = "靶点行数据")
    private List<TargetRowVO> rows;

    @Schema(description = "疾病列数据")
    private List<DiseaseColumnVO> columns;

    @Schema(description = "汇总信息")
    private MatrixSummaryVO summary;
}
