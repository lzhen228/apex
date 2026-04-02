package com.harbourbiomed.apex.progress.service.impl;

import com.harbourbiomed.apex.progress.mapper.ProgressMapper;
import com.harbourbiomed.apex.progress.request.DiseaseViewRequest;
import com.harbourbiomed.apex.progress.service.ProgressService;
import com.harbourbiomed.apex.progress.vo.DiseaseViewResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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

    @Override
    public void exportDiseaseView(DiseaseViewRequest req, HttpServletResponse response) throws IOException {
        List<String> targets = req.getTargets();
        if (targets == null || targets.isEmpty()) {
            targets = mapper.getTargetsByDisease(req.getDiseaseId())
                    .stream()
                    .map(m -> m.get("target").toString())
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> rows = mapper.exportProgressDrugPipeline(req.getDiseaseId(), targets);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("研发管线数据");

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Data style
            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Numeric style for score
            CellStyle numStyle = wb.createCellStyle();
            numStyle.cloneStyleFrom(dataStyle);
            DataFormat fmt = wb.createDataFormat();
            numStyle.setDataFormat(fmt.getFormat("0.0##"));

            String[] headers = {"药品英文名", "药品中文名", "靶点组合", "研发阶段", "阶段分值",
                    "原研机构", "所有研究机构", "最高阶段日期", "nctId", "疾病", "疾病领域", "是否联用"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            sheet.createFreezePane(0, 1);

            int rowIdx = 1;
            Set<String> seen = new LinkedHashSet<>();
            for (Map<String, Object> r : rows) {
                String key = str(r.get("drug_name_en")) + "|" + str(r.get("phase")) + "|" + str(r.get("disease_name"));
                if (!seen.add(key)) continue;

                Row dataRow = sheet.createRow(rowIdx++);
                String[] values = {
                        str(r.get("drug_name_en")),
                        str(r.get("drug_name_cn")),
                        str(r.get("targets_raw")),
                        str(r.get("phase")),
                        null, // score handled separately
                        str(r.get("originator")),
                        str(r.get("research_institute")),
                        str(r.get("phase_date")),
                        str(r.get("nct_id")),
                        str(r.get("disease_name")),
                        str(r.get("ta_name")),
                        str(r.get("is_combo")),
                };
                for (int i = 0; i < values.length; i++) {
                    Cell cell = dataRow.createCell(i);
                    if (i == 4) {
                        Object scoreObj = r.get("phase_score");
                        if (scoreObj != null) {
                            cell.setCellValue(Double.parseDouble(scoreObj.toString()));
                            cell.setCellStyle(numStyle);
                        } else {
                            cell.setCellValue("");
                            cell.setCellStyle(dataStyle);
                        }
                    } else {
                        cell.setCellValue(values[i] != null ? values[i] : "");
                        cell.setCellStyle(dataStyle);
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            String filename = "Apex_Target_Intelligence_swimlane_" + LocalDate.now() + ".xlsx";
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            wb.write(response.getOutputStream());
        }
    }
}
