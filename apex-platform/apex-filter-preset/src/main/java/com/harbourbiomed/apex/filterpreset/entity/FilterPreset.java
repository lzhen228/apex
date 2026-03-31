package com.harbourbiomed.apex.filterpreset.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.harbourbiomed.apex.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 筛选条件预设实体类
 * 
 * 对应数据库表：filter_preset
 * 
 * @author Harbour BioMed
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("filter_preset")
@Schema(description = "筛选条件预设实体")
public class FilterPreset extends BaseEntity {

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("name")
    @Schema(description = "预设名称")
    private String name;

    @TableField("module")
    @Schema(description = "模块标识：competition=靶点组合竞争格局，progress=靶点研发进展格局")
    private String module;

    @TableField("conditions")
    @Schema(description = "筛选条件JSON（疾病ID列表、阶段列表等）")
    private String conditions;

    @TableField("is_default")
    @Schema(description = "是否默认预设")
    private Boolean isDefault;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
