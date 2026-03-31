package com.harbourbiomed.apex.progress.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 靶点列表响应数据
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "靶点列表响应数据")
public class TargetListResponse {

    /**
     * 靶点统计列表
     */
    @Schema(description = "靶点统计列表")
    private List<TargetStatVO> targets;
}
