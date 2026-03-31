package com.harbourbiomed.apex.filterpreset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.harbourbiomed.apex.common.exception.BusinessException;
import com.harbourbiomed.apex.common.result.ErrorCode;
import com.harbourbiomed.apex.filterpreset.dto.SavePresetRequest;
import com.harbourbiomed.apex.filterpreset.entity.FilterPreset;
import com.harbourbiomed.apex.filterpreset.mapper.FilterPresetMapper;
import com.harbourbiomed.apex.filterpreset.service.FilterPresetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilterPresetServiceImpl implements FilterPresetService {

    private final FilterPresetMapper mapper;

    @Override
    public List<FilterPreset> listByModule(String module, Long userId) {
        return mapper.selectList(new LambdaQueryWrapper<FilterPreset>()
                .eq(FilterPreset::getUserId, userId.intValue())
                .eq(FilterPreset::getModule, module)
                .orderByDesc(FilterPreset::getCreatedAt));
    }

    @Override
    public FilterPreset save(SavePresetRequest req, Long userId) {
        FilterPreset preset = new FilterPreset();
        preset.setUserId(userId.intValue());
        preset.setName(req.getName());
        preset.setModule(req.getModule());
        preset.setConditions(req.getConditions());
        preset.setIsDefault(req.isDefault());
        preset.setCreatedAt(LocalDateTime.now());
        preset.setUpdatedAt(LocalDateTime.now());
        mapper.insert(preset);
        return preset;
    }

    @Override
    public void delete(Integer id, Long userId) {
        FilterPreset preset = mapper.selectById(id);
        if (preset == null || !preset.getUserId().equals(userId.intValue())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "预设不存在或无权删除");
        }
        mapper.deleteById(id);
    }
}
