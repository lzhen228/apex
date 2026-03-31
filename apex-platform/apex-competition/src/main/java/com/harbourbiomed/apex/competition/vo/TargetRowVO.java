package com.harbourbiomed.apex.competition.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 靶点行视图对象
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "靶点行视图对象")
public class TargetRowVO {

    @Schema(description = "靶点名称")
    private String targetName;

    @Schema(description = "该靶点的药物数量")
    private Integer drugCount;

    @Schema(description = "平均阶段分值")
    private Double avgPhaseScore;

    @Schema(description = "该靶点下的药物详情")
    private List<CellDrugVO> drugs;
}
