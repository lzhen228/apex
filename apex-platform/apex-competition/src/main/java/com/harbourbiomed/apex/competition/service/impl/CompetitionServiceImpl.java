package com.harbourbiomed.apex.competition.service.impl;

import com.harbourbiomed.apex.competition.mapper.CompetitionMapper;
import com.harbourbiomed.apex.competition.request.MatrixQueryRequest;
import com.harbourbiomed.apex.competition.service.CompetitionService;
import com.harbourbiomed.apex.competition.vo.CellDrugsResponse;
import com.harbourbiomed.apex.competition.vo.DiseaseTreeVO;
import com.harbourbiomed.apex.competition.vo.MatrixResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {

    private static final Map<String, String> PHASE_NAME_MAP = Map.ofEntries(
            Map.entry("批准上市", "Approved"),
            Map.entry("申请上市", "BLA"),
            Map.entry("III期临床", "Phase III"),
            Map.entry("II/III期临床", "Phase II/III"),
            Map.entry("II期临床", "Phase II"),
            Map.entry("I/II期临床", "Phase I/II"),
            Map.entry("I期临床", "Phase I"),
            Map.entry("申报临床", "IND"),
            Map.entry("临床前", "PreClinical")
    );

    private final CompetitionMapper mapper;

    @Override
    public List<DiseaseTreeVO> getDiseaseTree() {
        List<Map<String, Object>> rows = mapper.getDiseaseTree();

        Map<Integer, DiseaseTreeVO> taMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            int taId = toInt(row.get("ta_id"));
            String taName = str(row.get("ta_name"));
            int diseaseId = toInt(row.get("disease_id"));
            String diseaseName = str(row.get("disease_name"));

            taMap.computeIfAbsent(taId, k -> {
                DiseaseTreeVO ta = new DiseaseTreeVO();
                ta.setId(taId);
                ta.setLabel(taName);
                ta.setChildren(new ArrayList<>());
                return ta;
            });

            DiseaseTreeVO disease = new DiseaseTreeVO();
            disease.setId(diseaseId);
            disease.setLabel(diseaseName);
            taMap.get(taId).getChildren().add(disease);
        }
        return new ArrayList<>(taMap.values());
    }

    @Override
    public MatrixResponse queryMatrix(MatrixQueryRequest req) {
        List<String> normalizedPhases = normalizePhases(req.getPhases());
        List<Map<String, Object>> targetSummaries = mapper.queryTargetSummary(req.getDiseaseIds(), normalizedPhases);
        List<Map<String, Object>> rawRows = mapper.queryMatrixData(req.getDiseaseIds(), normalizedPhases);

        Map<String, Double> targetScoreMap = targetSummaries.stream()
                .collect(Collectors.toMap(
                        row -> str(row.get("target")),
                        row -> toDouble(row.get("max_score")),
                        Math::max,
                        LinkedHashMap::new
                ));

        Set<String> comboTargets = new HashSet<>();
        for (Map<String, Object> row : rawRows) {
            comboTargets.add(str(row.get("target_a")));
            comboTargets.add(str(row.get("target_b")));
        }

        List<String> orderedTargets = targetScoreMap.entrySet().stream()
                .filter(entry -> !req.isHideNoComboTargets() || comboTargets.contains(entry.getKey()))
                .sorted((a, b) -> {
                    int scoreCompare = Double.compare(b.getValue(), a.getValue());
                    return scoreCompare != 0 ? scoreCompare : a.getKey().compareToIgnoreCase(b.getKey());
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<MatrixResponse.ColumnVO> columns = orderedTargets.stream().map(target -> {
            MatrixResponse.ColumnVO col = new MatrixResponse.ColumnVO();
            col.setTarget(target);
            col.setMaxScore(targetScoreMap.getOrDefault(target, 0D));
            return col;
        }).collect(Collectors.toList());

        Map<String, Map<String, Object>> pairDataMap = new HashMap<>();
        for (Map<String, Object> row : rawRows) {
            String targetA = str(row.get("target_a"));
            String targetB = str(row.get("target_b"));
            pairDataMap.put(targetA + "|" + targetB, row);
        }

        List<MatrixResponse.RowVO> rows = new ArrayList<>();
        for (String rowTarget : orderedTargets) {
            double sumScore = 0;
            List<MatrixResponse.CellVO> cellVOs = new ArrayList<>();

            for (String colTarget : orderedTargets) {
                MatrixResponse.CellVO cellVO = new MatrixResponse.CellVO();
                cellVO.setTarget(colTarget);

                if (!rowTarget.equals(colTarget)) {
                    String key = canonicalPairKey(rowTarget, colTarget);
                    Map<String, Object> data = pairDataMap.get(key);
                    if (data != null) {
                        double score = toDouble(data.get("score"));
                        cellVO.setScore(score);
                        cellVO.setPhaseName(str(data.get("phase_name")));
                        cellVO.setDrugCount(toInt(data.get("drug_count")));
                        sumScore += score;
                    }
                }

                if (cellVO.getPhaseName() == null) {
                    cellVO.setScore(0D);
                    cellVO.setDrugCount(0);
                }
                cellVOs.add(cellVO);
            }

            MatrixResponse.RowVO rowVO = new MatrixResponse.RowVO();
            rowVO.setTarget(rowTarget);
            rowVO.setMaxScore(targetScoreMap.getOrDefault(rowTarget, 0D));
            rowVO.setSumScore(sumScore);
            rowVO.setCells(cellVOs);
            rows.add(rowVO);
        }

        MatrixResponse resp = new MatrixResponse();
        resp.setColumns(columns);
        resp.setRows(rows);
        resp.setTotalTargets(rows.size());
        resp.setTotalDiseases(req.getDiseaseIds().size());
        resp.setUpdatedAt(Optional.ofNullable(mapper.getLatestSyncTime()).orElse(""));
        return resp;
    }

    @Override
    public CellDrugsResponse getCellDrugs(String target, String pairTarget, List<Integer> diseaseIds, List<String> phases) {
        List<String> effectivePhases = (phases == null || phases.isEmpty())
                ? List.of("Approved", "BLA", "Phase III", "Phase II/III", "Phase II", "Phase I/II", "Phase I", "IND", "PreClinical")
            : normalizePhases(phases);

        List<Map<String, Object>> rows = mapper.getCellDrugs(target, pairTarget, diseaseIds, effectivePhases);

        List<CellDrugsResponse.DrugVO> drugs = rows.stream().map(row -> {
            CellDrugsResponse.DrugVO drug = new CellDrugsResponse.DrugVO();
            drug.setDrugNameEn(str(row.get("drug_name_en")));
            drug.setOriginator(str(row.get("originator")));
            drug.setResearchInstitute(str(row.get("research_institute")));
            drug.setHighestPhase(str(row.get("highest_phase")));
            drug.setHighestPhaseDate(str(row.get("highest_phase_date")));
            drug.setNctId(str(row.get("nct_id")));
            return drug;
        }).collect(Collectors.toList());

        CellDrugsResponse resp = new CellDrugsResponse();
        resp.setTarget(target);
        resp.setPairTarget(pairTarget);
        resp.setDrugs(drugs);
        return resp;
    }

    @Override
    public void exportMatrix(MatrixQueryRequest req, HttpServletResponse response) throws IOException {
        MatrixResponse matrix = queryMatrix(req);

        String filename = "competition_matrix_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" +
                URLEncoder.encode(filename, StandardCharsets.UTF_8));

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("竞争格局");

            // Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("靶点");
            header.createCell(1).setCellValue("Highest Phase");
            for (int i = 0; i < matrix.getColumns().size(); i++) {
                header.createCell(i + 2).setCellValue(matrix.getColumns().get(i).getTarget());
            }

            // Data rows
            int rowIdx = 1;
            for (MatrixResponse.RowVO row : matrix.getRows()) {
                Row excelRow = sheet.createRow(rowIdx++);
                excelRow.createCell(0).setCellValue(row.getTarget());
                excelRow.createCell(1).setCellValue(row.getMaxScore());
                Map<String, MatrixResponse.CellVO> cellMap = new HashMap<>();
                for (MatrixResponse.CellVO cell : row.getCells()) {
                    cellMap.put(cell.getTarget(), cell);
                }
                for (int i = 0; i < matrix.getColumns().size(); i++) {
                    String colTarget = matrix.getColumns().get(i).getTarget();
                    MatrixResponse.CellVO cell = cellMap.get(colTarget);
                    String val = (cell != null && cell.getPhaseName() != null) ? cell.getPhaseName() : "";
                    excelRow.createCell(i + 2).setCellValue(val);
                }
            }

            wb.write(response.getOutputStream());
        }
    }

    private int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return n.intValue();
        return Integer.parseInt(o.toString());
    }

    private double toDouble(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return n.doubleValue();
        return Double.parseDouble(o.toString());
    }

    private List<String> normalizePhases(List<String> phases) {
        if (phases == null || phases.isEmpty()) {
            return List.of("Approved", "BLA", "Phase III", "Phase II/III", "Phase II", "Phase I/II", "Phase I", "IND", "PreClinical");
        }
        return phases.stream()
                .filter(Objects::nonNull)
                .map(phase -> PHASE_NAME_MAP.getOrDefault(phase, phase))
                .distinct()
                .collect(Collectors.toList());
    }

    private String canonicalPairKey(String left, String right) {
        if (left.compareToIgnoreCase(right) <= 0) {
            return left + "|" + right;
        }
        return right + "|" + left;
    }

    private String str(Object o) {
        return o == null ? null : o.toString();
    }
}
