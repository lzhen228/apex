package com.harbourbiomed.apex.competition.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MatrixQueryRequest {

    @NotEmpty(message = "请至少选择一个疾病")
    private List<Integer> diseaseIds;

    @NotEmpty(message = "请至少选择一个研发阶段")
    private List<String> phases;

    private boolean hideNoComboTargets = false;
}
