package com.harbourbiomed.apex.progress.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 疾病视图响应数据
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "疾病视图响应数据")
public class DiseaseViewResponse {

    /**
     * 疾病信息
     */
    @Schema(description = "疾病信息")
    private DiseaseInfoVO disease;

    /**
     * 靶点管线列表
     */
    @Schema(description = "靶点管线列表")
    private List<TargetPipelineVO> pipelines;
}
