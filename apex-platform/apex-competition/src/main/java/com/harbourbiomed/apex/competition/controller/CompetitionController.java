package com.harbourbiomed.apex.competition.controller;

import com.harbourbiomed.apex.common.result.Result;
import com.harbourbiomed.apex.competition.request.MatrixQueryRequest;
import com.harbourbiomed.apex.competition.service.CompetitionService;
import com.harbourbiomed.apex.competition.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 竞争格局控制器
 *
 * @author Harbour BioMed
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "竞争格局管理", description = "靶点组合竞争格局相关接口")
public class CompetitionController {

    private final CompetitionService competitionService;

    @GetMapping("/diseases/tree")
    @Operation(summary = "获取疾病树形结构", description = "返回治疗领域和疾病的树形结构")
    public Result<List<TherapeuticAreaVO>> getDiseaseTree() {
        List<TherapeuticAreaVO> tree = competitionService.getDiseaseTree();
        return Result.success(tree);
    }

    @PostMapping("/competition/matrix")
    @Operation(summary = "查询竞争格局矩阵", description = "根据筛选条件查询靶点组合竞争格局数据")
    public Result<MatrixResponse> queryMatrix(@RequestBody MatrixQueryRequest request) {
        MatrixResponse response = competitionService.queryMatrix(request);
        return Result.success(response);
    }

    @GetMapping("/competition/cell-drugs")
    @Operation(summary = "获取单元格药物详情", description = "获取指定靶点和疾病下的药物详情列表")
    public Result<CellDrugsResponse> getCellDrugs(
            @RequestParam("target") String target,
            @RequestParam("diseaseId") Integer diseaseId,
            @RequestParam(value = "phases", required = false) String phases
    ) {
        List<String> phaseList = null;
        if (phases != null && !phases.isEmpty()) {
            phaseList = List.of(phases.split(","));
        }

        CellDrugsResponse response = competitionService.getCellDrugs(target, diseaseId, phaseList);
        return Result.success(response);
    }

    @PostMapping("/competition/export")
    @Operation(summary = "导出竞争格局矩阵", description = "导出竞争格局数据为 Excel 文件")
    public void exportMatrix(@RequestBody MatrixQueryRequest request,
                             HttpServletResponse response) throws IOException {
        competitionService.exportMatrix(request, response);
    }
}
