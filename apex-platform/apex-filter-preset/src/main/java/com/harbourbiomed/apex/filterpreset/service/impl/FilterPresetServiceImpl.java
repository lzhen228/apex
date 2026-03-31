package com.harbourbiomed.apex.filterpreset.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.harbourbiomed.apex.common.exception.BusinessException;
import com.harbourbiomed.apex.filterpreset.dto.PresetListResponse;
import com.harbourbiomed.apex.filterpreset.dto.SavePresetRequest;
import com.harbourbiomed.apex.filterpreset.entity.FilterPreset;
import com.harbourbiomed.apex.filterpreset.mapper.FilterPresetMapper;
import com.harbourbiomed.apex.filterpreset.service.FilterPresetService;
import com.harbourbiomed.apex.filterpreset.vo.FilterPresetVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 筛选条件预设服务实现类
 * 
 * @author Harbour BioMed
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FilterPresetServiceImpl implements FilterPresetService {

    private final FilterPresetMapper filterPresetMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FilterPresetVO savePreset(Long userId, SavePresetRequest request) {
        log.info("保存筛选条件预设，用户ID: {}, 预设名称: {}, 模块: {}", userId, request.getName(), request.getModule());

        // 设置默认值
        Boolean isDefault = request.getIsDefault() != null ? request.getIsDefault() : false;
        String conditionsJson = JSON.toJSONString(request.getConditions());

        // 如果设置为默认，将该用户在该模块下的其他预设设为非默认
        if (isDefault) {
            LambdaUpdateWrapper<FilterPreset> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(FilterPreset::getUserId, userId)
                        .eq(FilterPreset::getModule, request.getModule())
                        .eq(FilterPreset::getIsDefault, true)
                        .set(FilterPreset::getIsDefault, false);
            filterPresetMapper.update(null, updateWrapper);
            log.info("已将用户 {} 在模块 {} 下的其他预设设为非默认", userId, request.getModule());
        }

        // 检查是否存在同名预设（同一用户、同一模块）
        LambdaQueryWrapper<FilterPreset> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FilterPreset::getUserId, userId)
                   .eq(FilterPreset::getModule, request.getModule())
                   .eq(FilterPreset::getName, request.getName());
        FilterPreset existingPreset = filterPresetMapper.selectOne(queryWrapper);

        FilterPreset filterPreset;
        if (existingPreset != null) {
            // 更新现有预设
            log.info("更新现有预设，ID: {}", existingPreset.getId());
            filterPreset = existingPreset;
            filterPreset.setConditions(conditionsJson);
            filterPreset.setIsDefault(isDefault);
            filterPresetMapper.updateById(filterPreset);
        } else {
            // 创建新预设
            log.info("创建新预设");
            filterPreset = new FilterPreset();
            filterPreset.setUserId(userId);
            filterPreset.setName(request.getName());
            filterPreset.setModule(request.getModule());
            filterPreset.setConditions(conditionsJson);
            filterPreset.setIsDefault(isDefault);
            filterPresetMapper.insert(filterPreset);
        }

        return convertToVO(filterPreset);
    }

    @Override
    public PresetListResponse getPresetsByUser(Long userId, String module) {
        log.info("获取用户 {} 在模块 {} 下的预设列表", userId, module);

        LambdaQueryWrapper<FilterPreset> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FilterPreset::getUserId, userId)
                   .eq(FilterPreset::getModule, module)
                   .orderByDesc(FilterPreset::getCreatedAt);

        List<FilterPreset> presetList = filterPresetMapper.selectList(queryWrapper);

        List<FilterPresetVO> presetVOs = presetList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PresetListResponse response = new PresetListResponse();
        response.setPresets(presetVOs);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePreset(Long userId, Long id) {
        log.info("删除预设，用户ID: {}, 预设ID: {}", userId, id);

        // 检查预设是否存在且属于当前用户
        FilterPreset preset = filterPresetMapper.selectById(id);
        if (preset == null) {
            throw new BusinessException("预设不存在");
        }

        if (!preset.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该预设");
        }

        // 删除预设
        filterPresetMapper.deleteById(id);
        log.info("预设删除成功，ID: {}", id);
    }

    /**
     * 将实体转换为视图对象
     */
    private FilterPresetVO convertToVO(FilterPreset filterPreset) {
        FilterPresetVO vo = new FilterPresetVO();
        vo.setId(filterPreset.getId());
        vo.setName(filterPreset.getName());
        vo.setModule(filterPreset.getModule());
        JsonNode conditions = objectMapper.valueToTree(JSON.parse(filterPreset.getConditions()));
        vo.setConditions(conditions);
        vo.setIsDefault(filterPreset.getIsDefault());
        vo.setCreatedAt(filterPreset.getCreatedAt());
        vo.setUpdatedAt(filterPreset.getUpdatedAt());
        return vo;
    }
}
