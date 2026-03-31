package com.harbourbiomed.apex.filterpreset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "filter_preset", autoResultMap = true)
public class FilterPreset {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private String name;
    private String module;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object conditions;

    private Boolean isDefault;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
