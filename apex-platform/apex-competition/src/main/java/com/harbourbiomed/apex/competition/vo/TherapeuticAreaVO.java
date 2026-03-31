package com.harbourbiomed.apex.competition.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 治疗领域视图对象
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "治疗领域视图对象")
public class TherapeuticAreaVO {

    @Schema(description = "治疗领域 ID")
    private Integer id;

    @Schema(description = "英文名称")
    private String nameEn;

    @Schema(description = "中文名称")
    private String nameCn;

    @Schema(description = "关联的疾病列表")
    private List<DiseaseVO> diseases;
}
