package com.harbourbiomed.apex.progress.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 靶点管线值对象
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "靶点管线值对象")
public class TargetPipelineVO {

    /**
     * 靶点名称
     */
    @Schema(description = "靶点名称")
    private String targetName;

    /**
     * 药物数量
     */
    @Schema(description = "药物数量")
    private Integer drugCount;

    /**
     * 平均阶段分值
     */
    @Schema(description = "平均阶段分值")
    private Double avgPhaseScore;

    /**
     * 药物卡片列表
     */
    @Schema(description = "药物卡片列表")
    private List<DrugCardVO> drugs;
}
