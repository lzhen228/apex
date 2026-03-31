package com.harbourbiomed.apex.competition.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 治疗领域实体
 *
 * @author Harbour BioMed
 */
@Data
@TableName("therapeutic_area")
@Schema(description = "治疗领域实体")
public class TherapeuticArea implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    @Schema(description = "治疗领域 ID")
    private Integer id;

    @TableField("name_en")
    @Schema(description = "英文名称")
    private String nameEn;

    @TableField("name_cn")
    @Schema(description = "中文名称")
    private String nameCn;

    @TableField("sort_order")
    @Schema(description = "排序顺序")
    private Integer sortOrder;

    /**
     * 关联的疾病列表（非数据库字段，用于树形结构返回）
     */
    @TableField(exist = false)
    @Schema(description = "关联的疾病列表")
    private List<Disease> diseases;
}
