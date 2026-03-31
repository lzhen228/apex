package com.harbourbiomed.apex.competition.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 疾病实体
 *
 * @author Harbour BioMed
 */
@Data
@TableName("disease")
@Schema(description = "疾病实体")
public class Disease implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    @Schema(description = "疾病 ID")
    private Integer id;

    @TableField("ta_id")
    @Schema(description = "治疗领域 ID")
    private Integer taId;

    @TableField("name_en")
    @Schema(description = "英文名称")
    private String nameEn;

    @TableField("name_cn")
    @Schema(description = "中文名称")
    private String nameCn;

    @TableField("abbreviation")
    @Schema(description = "缩写/简称")
    private String abbreviation;
}
