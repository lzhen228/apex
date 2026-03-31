package com.harbourbiomed.apex.filterpreset.controller;

import com.harbourbiomed.apex.common.result.Result;
import com.harbourbiomed.apex.filterpreset.dto.SavePresetRequest;
import com.harbourbiomed.apex.filterpreset.entity.FilterPreset;
import com.harbourbiomed.apex.filterpreset.service.FilterPresetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.harbourbiomed.apex.common.security.ApexUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "筛选条件预设")
@RestController
@RequestMapping("/v1/filter-presets")
@RequiredArgsConstructor
public class FilterPresetController {

    private final FilterPresetService presetService;

    @Operation(summary = "获取预设列表")
    @GetMapping
    public Result<List<FilterPreset>> list(@RequestParam String module,
                                           @AuthenticationPrincipal ApexUserDetails userDetails) {
        return Result.ok(presetService.listByModule(module, userDetails.getUserId()));
    }

    @Operation(summary = "保存预设")
    @PostMapping
    public Result<FilterPreset> save(@Valid @RequestBody SavePresetRequest req,
                                     @AuthenticationPrincipal ApexUserDetails userDetails) {
        return Result.ok(presetService.save(req, userDetails.getUserId()));
    }

    @Operation(summary = "删除预设")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id,
                                @AuthenticationPrincipal ApexUserDetails userDetails) {
        presetService.delete(id, userDetails.getUserId());
        return Result.ok(null);
    }
}
