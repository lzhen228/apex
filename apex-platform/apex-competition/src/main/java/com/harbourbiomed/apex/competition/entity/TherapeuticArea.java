package com.harbourbiomed.apex.competition.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("therapeutic_area")
public class TherapeuticArea {
    private Integer id;
    private String nameEn;
    private String nameCn;
    private Integer sortOrder;
}
