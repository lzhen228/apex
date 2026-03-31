package com.harbourbiomed.apex.filterpreset.dto;

import com.harbourbiomed.apex.filterpreset.vo.FilterPresetVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 筛选条件预设列表响应
 * 
 * @author Harbour BioMed
 */
@Data
@Schema(description = "筛选条件预设列表响应")
public class PresetListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "预设列表")
    private List<FilterPresetVO> presets;
}
