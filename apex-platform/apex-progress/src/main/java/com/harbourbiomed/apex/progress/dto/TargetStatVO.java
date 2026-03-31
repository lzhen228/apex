package com.harbourbiomed.apex.progress.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 靶点统计值对象
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "靶点统计值对象")
public class TargetStatVO {

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
     * 最高阶段
     */
    @Schema(description = "最高阶段")
    private String highestPhase;
}
