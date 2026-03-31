package com.harbourbiomed.apex.filterpreset.controller;

import com.harbourbiomed.apex.common.result.Result;
import com.harbourbiomed.apex.filterpreset.dto.PresetListResponse;
import com.harbourbiomed.apex.filterpreset.dto.SavePresetRequest;
import com.harbourbiomed.apex.filterpreset.service.FilterPresetService;
import com.harbourbiomed.apex.filterpreset.util.SecurityUtil;
import com.harbourbiomed.apex.filterpreset.vo.FilterPresetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 筛选条件预设控制器
 * 
 * @author Harbour BioMed
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/filter-presets")
@RequiredArgsConstructor
@Tag(name = "筛选条件预设管理", description = "筛选条件预设的增删改查接口")
public class FilterPresetController {

    private final FilterPresetService filterPresetService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @Operation(summary = "保存筛选条件预设", description = "保存或更新筛选条件预设，支持设为默认")
    public Result<FilterPresetVO> savePreset(
            @Valid @RequestBody SavePresetRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = securityUtil.getUserIdFromRequest(httpRequest);
        log.info("收到保存筛选条件预设请求，用户ID: {}, 预设名称: {}", userId, request.getName());
        
        FilterPresetVO result = filterPresetService.savePreset(userId, request);
        return Result.success(result);
    }

    @GetMapping
    @Operation(summary = "获取筛选条件预设列表", description = "获取当前用户指定模块的所有筛选条件预设")
    public Result<PresetListResponse> getPresets(
            @Parameter(description = "模块标识：competition=靶点组合竞争格局，progress=靶点研发进展格局", 
                      required = true, 
                      example = "competition")
            @RequestParam("module") String module,
            HttpServletRequest httpRequest) {
        
        Long userId = securityUtil.getUserIdFromRequest(httpRequest);
        log.info("收到获取预设列表请求，用户ID: {}, 模块: {}", userId, module);
        
        PresetListResponse result = filterPresetService.getPresetsByUser(userId, module);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除筛选条件预设", description = "删除指定的筛选条件预设")
    public Result<Void> deletePreset(
            @Parameter(description = "预设ID", required = true, example = "1")
            @PathVariable("id") Long id,
            HttpServletRequest httpRequest) {
        
        Long userId = securityUtil.getUserIdFromRequest(httpRequest);
        log.info("收到删除预设请求，用户ID: {}, 预设ID: {}", userId, id);
        
        filterPresetService.deletePreset(userId, id);
        return Result.<Void>success("删除成功", null);
    }
}
