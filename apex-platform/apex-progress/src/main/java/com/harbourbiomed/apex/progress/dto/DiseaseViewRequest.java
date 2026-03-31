package com.harbourbiomed.apex.progress.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 疾病视图请求参数
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "疾病视图请求参数")
public class DiseaseViewRequest {

    /**
     * 疾病 ID（必需）
     */
    @NotNull(message = "疾病 ID 不能为空")
    @Schema(description = "疾病 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer diseaseId;

    /**
     * 靶点筛选（可选，不传则返回所有）
     */
    @Schema(description = "靶点筛选列表")
    private List<String> targets;

    /**
     * 研发阶段筛选（可选）
     */
    @Schema(description = "研发阶段筛选列表")
    private List<String> phases;

    /**
     * 起源公司筛选（可选）
     */
    @Schema(description = "起源公司筛选列表")
    private List<String> origins;

    /**
     * 排序字段：targetName|drugCount|avgPhaseScore
     */
    @Schema(description = "排序字段", allowableValues = {"targetName", "drugCount", "avgPhaseScore"})
    private String sortBy;

    /**
     * 排序方向：ASC|DESC
     */
    @Schema(description = "排序方向", allowableValues = {"ASC", "DESC"})
    private String sortOrder;
}
