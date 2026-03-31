package com.harbourbiomed.apex.filterpreset.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 筛选条件预设视图对象
 * 
 * @author Harbour BioMed
 */
@Data
@Schema(description = "筛选条件预设视图对象")
public class FilterPresetVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "预设ID")
    private Long id;

    @Schema(description = "预设名称")
    private String name;

    @Schema(description = "模块标识")
    private String module;

    @Schema(description = "筛选条件JSON")
    private JsonNode conditions;

    @Schema(description = "是否默认预设")
    private Boolean isDefault;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
