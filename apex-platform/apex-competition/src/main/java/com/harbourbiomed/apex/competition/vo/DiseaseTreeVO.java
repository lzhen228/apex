package com.harbourbiomed.apex.competition.vo;

import lombok.Data;

import java.util.List;

@Data
public class DiseaseTreeVO {
    private Integer id;
    private String label;
    private List<DiseaseTreeVO> children;
}
