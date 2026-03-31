package com.harbourbiomed.apex.competition.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("disease")
public class Disease {
    private Integer id;
    private Integer taId;
    private String nameEn;
    private String nameCn;
    private String abbreviation;
}
