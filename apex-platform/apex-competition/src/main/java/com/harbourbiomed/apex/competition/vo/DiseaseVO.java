package com.harbourbiomed.apex.competition.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 疾病视图对象
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "疾病视图对象")
public class DiseaseVO {

    @Schema(description = "疾病 ID")
    private Integer id;

    @Schema(description = "英文名称")
    private String nameEn;

    @Schema(description = "中文名称")
    private String nameCn;

    @Schema(description = "缩写/简称")
    private String abbreviation;

    @Schema(description = "治疗领域 ID")
    private Integer taId;
}
