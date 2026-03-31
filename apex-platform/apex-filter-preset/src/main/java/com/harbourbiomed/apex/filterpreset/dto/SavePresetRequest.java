package com.harbourbiomed.apex.filterpreset.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class SavePresetRequest {

    @NotBlank(message = "预设名称不能为空")
    private String name;

    @NotBlank(message = "模块不能为空")
    private String module;

    private Map<String, Object> conditions;

    private boolean isDefault = false;
}
