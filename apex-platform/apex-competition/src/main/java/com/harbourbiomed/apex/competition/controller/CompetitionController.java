package com.harbourbiomed.apex.competition.controller;

import com.harbourbiomed.apex.competition.request.MatrixQueryRequest;
import com.harbourbiomed.apex.competition.service.CompetitionService;
import com.harbourbiomed.apex.competition.vo.CellDrugsResponse;
import com.harbourbiomed.apex.competition.vo.DiseaseTreeVO;
import com.harbourbiomed.apex.competition.vo.MatrixResponse;
import com.harbourbiomed.apex.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "靶点组合竞争格局")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;

    @Operation(summary = "疾病树（治疗领域→疾病）")
    @GetMapping("/diseases/tree")
    public Result<List<DiseaseTreeVO>> getDiseaseTree() {
        return Result.ok(competitionService.getDiseaseTree());
    }

    @Operation(summary = "矩阵查询")
    @PostMapping("/competition/matrix")
    public Result<MatrixResponse> queryMatrix(@Valid @RequestBody MatrixQueryRequest req) {
        return Result.ok(competitionService.queryMatrix(req));
    }

    @Operation(summary = "格子药品详情")
    @GetMapping("/competition/cell-drugs")
    public Result<CellDrugsResponse> getCellDrugs(
            @RequestParam String target,
            @RequestParam String pairTarget,
            @RequestParam List<Integer> diseaseIds,
            @RequestParam(required = false) List<String> phases) {
        return Result.ok(competitionService.getCellDrugs(target, pairTarget, diseaseIds, phases));
    }

    @Operation(summary = "导出 Excel")
    @PostMapping("/competition/export")
    public void exportMatrix(@Valid @RequestBody MatrixQueryRequest req,
                             HttpServletResponse response) throws IOException {
        competitionService.exportMatrix(req, response);
    }
}
