package com.harbourbiomed.apex.progress.controller;

import com.harbourbiomed.apex.common.result.Result;
import com.harbourbiomed.apex.progress.dto.DiseaseViewRequest;
import com.harbourbiomed.apex.progress.dto.DiseaseViewResponse;
import com.harbourbiomed.apex.progress.dto.TargetListResponse;
import com.harbourbiomed.apex.progress.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 进展控制器
 *
 * @author Harbour BioMed
 */
@Tag(name = "靶点研发进展", description = "靶点研发进展格局接口")
@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    /**
     * 查询靶点列表
     *
     * @param diseaseId 疾病 ID
     * @return 靶点列表
     */
    @Operation(summary = "查询靶点列表", description = "根据疾病 ID 查询所有相关的靶点列表，带药物数量统计")
    @GetMapping("/targets")
    public Result<TargetListResponse> getTargets(
            @Parameter(description = "疾病 ID", required = true, example = "203")
            @RequestParam("diseaseId") Integer diseaseId) {

        TargetListResponse response = progressService.getTargetsByDisease(diseaseId);
        return Result.success(response);
    }

    /**
     * 查询疾病研发进展视图
     *
     * @param request 请求参数
     * @return 研发进展视图
     */
    @Operation(summary = "查询疾病研发进展视图", description = "查询指定疾病下各靶点的研发进展管线数据")
    @PostMapping("/disease-view")
    public Result<DiseaseViewResponse> getDiseaseView(
            @Valid @RequestBody DiseaseViewRequest request) {

        DiseaseViewResponse response = progressService.getDiseaseView(request);
        return Result.success(response);
    }
}
