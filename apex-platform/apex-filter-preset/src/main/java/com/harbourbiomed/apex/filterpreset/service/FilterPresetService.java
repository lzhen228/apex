package com.harbourbiomed.apex.filterpreset.service;

import com.harbourbiomed.apex.filterpreset.dto.PresetListResponse;
import com.harbourbiomed.apex.filterpreset.dto.SavePresetRequest;
import com.harbourbiomed.apex.filterpreset.vo.FilterPresetVO;

/**
 * 筛选条件预设服务接口
 * 
 * @author Harbour BioMed
 */
public interface FilterPresetService {

    /**
     * 保存或更新筛选条件预设
     * 
     * @param userId 当前用户ID
     * @param request 保存请求参数
     * @return 保存后的预设视图对象
     */
    FilterPresetVO savePreset(Long userId, SavePresetRequest request);

    /**
     * 获取用户指定模块的预设列表
     * 
     * @param userId 当前用户ID
     * @param module 模块标识（competition 或 progress）
     * @return 预设列表响应
     */
    PresetListResponse getPresetsByUser(Long userId, String module);

    /**
     * 删除筛选条件预设
     * 
     * @param userId 当前用户ID
     * @param id 预设ID
     */
    void deletePreset(Long userId, Long id);
}
