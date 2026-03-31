package com.harbourbiomed.apex.competition.vo;

import lombok.Data;

import java.util.List;

@Data
public class MatrixResponse {
    private List<ColumnVO> columns;
    private List<RowVO> rows;
    private int totalTargets;
    private int totalDiseases;
    private String updatedAt;

    @Data
    public static class ColumnVO {
        private String target;
        private double maxScore;
    }

    @Data
    public static class RowVO {
        private String target;
        private double maxScore;
        private double sumScore;
        private List<CellVO> cells;
    }

    @Data
    public static class CellVO {
        private String target;
        private double score;
        private String phaseName;
        private int drugCount;
    }
}
