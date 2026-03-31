package com.harbourbiomed.apex.filterpreset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 保存筛选条件预设请求参数
 * 
 * @author Harbour BioMed
 */
@Data
@Schema(description = "保存筛选条件预设请求参数")
public class SavePresetRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "预设名称不能为空")
    @Size(min = 1, max = 100, message = "预设名称长度必须在1-100之间")
    @Schema(description = "预设名称", example = "TNF 靶点筛选")
    private String name;

    @NotBlank(message = "模块不能为空")
    @Pattern(regexp = "competition|progress", message = "模块必须是 competition 或 progress")
    @Schema(description = "模块标识", example = "competition", allowableValues = {"competition", "progress"})
    private String module;

    @NotNull(message = "筛选条件不能为空")
    @Schema(description = "筛选条件JSON")
    private Map<String, Object> conditions;

    @Schema(description = "是否设为默认预设", example = "false")
    private Boolean isDefault;
}
