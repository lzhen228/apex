package com.harbourbiomed.apex.progress.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 疾病信息值对象
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "疾病信息值对象")
public class DiseaseInfoVO {

    /**
     * 疾病 ID
     */
    @Schema(description = "疾病 ID")
    private Integer id;

    /**
     * 疾病英文名称
     */
    @Schema(description = "疾病英文名称")
    private String nameEn;

    /**
     * 疾病中文名称
     */
    @Schema(description = "疾病中文名称")
    private String nameCn;

    /**
     * 治疗领域 ID
     */
    @Schema(description = "治疗领域 ID")
    private Integer taId;
}
