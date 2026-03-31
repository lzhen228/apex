package com.harbourbiomed.apex.progress.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DiseaseViewRequest {

    @NotNull(message = "疾病必选")
    private Integer diseaseId;

    private List<String> targets;
}
