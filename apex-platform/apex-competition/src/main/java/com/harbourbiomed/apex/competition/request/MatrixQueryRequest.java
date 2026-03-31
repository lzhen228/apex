package com.harbourbiomed.apex.competition.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 矩阵查询请求
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "竞争格局矩阵查询请求")
public class MatrixQueryRequest {

    @Schema(description = "疾病 ID 列表")
    private List<Integer> diseaseIds;

    @Schema(description = "靶点列表")
    private List<String> targets;

    @Schema(description = "研发阶段筛选")
    private List<String> phases;

    @Schema(description = "起源公司筛选")
    private List<String> origins;

    @Schema(description = "是否排除自身（0=否，1=是）")
    private Integer excludeSelf;

    @Schema(description = "MOA 关键词筛选")
    private List<String> moaKeywords;
}
