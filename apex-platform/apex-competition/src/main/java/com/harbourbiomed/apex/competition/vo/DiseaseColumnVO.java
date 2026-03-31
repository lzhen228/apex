package com.harbourbiomed.apex.competition.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 疾病列视图对象
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "疾病列视图对象")
public class DiseaseColumnVO {

    @Schema(description = "疾病 ID")
    private Integer diseaseId;

    @Schema(description = "疾病英文名称")
    private String diseaseNameEn;

    @Schema(description = "疾病中文名称")
    private String diseaseNameCn;
}
