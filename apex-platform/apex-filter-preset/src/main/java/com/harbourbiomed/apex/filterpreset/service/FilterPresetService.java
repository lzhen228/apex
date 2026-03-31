package com.harbourbiomed.apex.filterpreset.service;

import com.harbourbiomed.apex.filterpreset.dto.SavePresetRequest;
import com.harbourbiomed.apex.filterpreset.entity.FilterPreset;

import java.util.List;

public interface FilterPresetService {

    List<FilterPreset> listByModule(String module, Long userId);

    FilterPreset save(SavePresetRequest req, Long userId);

    void delete(Integer id, Long userId);
}
