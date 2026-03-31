package com.harbourbiomed.apex.progress.controller;

import com.harbourbiomed.apex.common.result.Result;
import com.harbourbiomed.apex.progress.request.DiseaseViewRequest;
import com.harbourbiomed.apex.progress.service.ProgressService;
import com.harbourbiomed.apex.progress.vo.DiseaseViewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "靶点研发进展格局")
@RestController
@RequestMapping("/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @Operation(summary = "获取疾病下靶点列表")
    @GetMapping("/targets")
    public Result<List<Map<String, Object>>> getTargets(@RequestParam Integer diseaseId) {
        return Result.ok(progressService.getTargetsByDisease(diseaseId));
    }

    @Operation(summary = "疾病视图")
    @PostMapping("/disease-view")
    public Result<DiseaseViewResponse> getDiseaseView(@Valid @RequestBody DiseaseViewRequest req) {
        return Result.ok(progressService.getDiseaseView(req));
    }
}
