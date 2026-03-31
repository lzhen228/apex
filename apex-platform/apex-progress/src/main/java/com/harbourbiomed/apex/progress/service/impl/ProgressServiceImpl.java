package com.harbourbiomed.apex.progress.service.impl;

import com.harbourbiomed.apex.progress.mapper.ProgressMapper;
import com.harbourbiomed.apex.progress.request.DiseaseViewRequest;
import com.harbourbiomed.apex.progress.service.ProgressService;
import com.harbourbiomed.apex.progress.vo.DiseaseViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private static final List<String> PHASE_ORDER = List.of(
            "PreClinical", "IND", "Phase I", "Phase I/II",
            "Phase II", "Phase II/III", "Phase III", "BLA", "Approved");

    private final ProgressMapper mapper;

    @Override
    public List<Map<String, Object>> getTargetsByDisease(Integer diseaseId) {
        return mapper.getTargetsByDisease(diseaseId);
    }

    @Override
    public DiseaseViewResponse getDiseaseView(DiseaseViewRequest req) {
        List<String> targets = req.getTargets();
        if (targets == null || targets.isEmpty()) {
            // Default: all targets for this disease
            targets = mapper.getTargetsByDisease(req.getDiseaseId())
                    .stream()
                    .map(m -> m.get("target").toString())
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> rows = mapper.getDiseaseViewData(req.getDiseaseId(), targets);
        String diseaseName = mapper.getDiseaseName(req.getDiseaseId());

        // Group by target → phase → list of drugs
        Map<String, Map<String, List<DiseaseViewResponse.DrugVO>>> grouped = new LinkedHashMap<>();
        for (String t : targets) {
            grouped.put(t, new LinkedHashMap<>());
        }

        int totalDrugs = 0;
        Set<String> drugKeys = new HashSet<>();

        for (Map<String, Object> row : rows) {
            String target = str(row.get("target"));
            String phase = str(row.get("phase"));
            String drugName = str(row.get("drug_name_en"));

            if (!grouped.containsKey(target)) continue;

            String key = target + "|" + drugName + "|" + phase;
            if (drugKeys.contains(key)) continue;
            drugKeys.add(key);

            DiseaseViewResponse.DrugVO drug = new DiseaseViewResponse.DrugVO();
            drug.setDrugNameEn(drugName);
            drug.setOriginator(str(row.get("originator")));
            drug.setResearchInstitute(str(row.get("research_institute")));
            drug.setHighestPhaseDate(str(row.get("highest_phase_date")));
            drug.setNctId(str(row.get("nct_id")));

            grouped.get(target)
                   .computeIfAbsent(phase, k -> new ArrayList<>())
                   .add(drug);
            totalDrugs++;
        }

        List<DiseaseViewResponse.TargetRowVO> targetRows = grouped.entrySet().stream()
                .map(e -> {
                    DiseaseViewResponse.TargetRowVO row = new DiseaseViewResponse.TargetRowVO();
                    row.setTarget(e.getKey());
                    row.setPhaseDrugs(e.getValue());
                    return row;
                })
                .filter(r -> !r.getPhaseDrugs().isEmpty())
                .collect(Collectors.toList());

        DiseaseViewResponse resp = new DiseaseViewResponse();
        resp.setDiseaseName(diseaseName);
        resp.setPhases(PHASE_ORDER);
        resp.setTargetRows(targetRows);
        resp.setTotalDrugs(totalDrugs);
        resp.setUpdatedAt(Optional.ofNullable(mapper.getLatestSyncTime()).orElse(""));
        return resp;
    }

    private String str(Object o) {
        return o == null ? null : o.toString();
    }
}
