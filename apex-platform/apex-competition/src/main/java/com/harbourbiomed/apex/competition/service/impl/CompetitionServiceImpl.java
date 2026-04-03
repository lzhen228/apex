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
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
        // 三个独立查询并发执行，减少串行等待时间
        CompletableFuture<List<Map<String, Object>>> summaryFuture = CompletableFuture.supplyAsync(
                () -> mapper.queryTargetSummary(req.getDiseaseIds(), normalizedPhases));
        CompletableFuture<List<Map<String, Object>>> matrixFuture = CompletableFuture.supplyAsync(
                () -> mapper.queryMatrixData(req.getDiseaseIds(), normalizedPhases));
        CompletableFuture<String> syncTimeFuture = CompletableFuture.supplyAsync(
                () -> mapper.getLatestSyncTime());
        List<Map<String, Object>> targetSummaries = summaryFuture.join();
        List<Map<String, Object>> rawRows = matrixFuture.join();

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
        resp.setUpdatedAt(Optional.ofNullable(syncTimeFuture.join()).orElse(""));
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
            drug.setMoa(str(row.get("moa")));
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
        List<String> normalizedPhases = normalizePhases(req.getPhases());
        List<Map<String, Object>> rows = mapper.exportDrugPipeline(req.getDiseaseIds(), normalizedPhases);

        String filename = "Apex_Target_Intelligence_matrix_" + LocalDate.now() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" +
                URLEncoder.encode(filename, StandardCharsets.UTF_8));

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("研发管线数据");

            // ── 样式 ─────────────────────────────────────────────
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle numStyle = wb.createCellStyle();
            DataFormat fmt = wb.createDataFormat();
            numStyle.setDataFormat(fmt.getFormat("0.0"));

            // ── 表头 ─────────────────────────────────────────────
            String[] headers = { "药品英文名", "药品中文名", "靶点组合", "研发阶段", "阶段分值",
                    "原研机构", "所有研究机构", "最高阶段日期", "nctId", "疾病", "疾病领域", "是否联用" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── 数据行 ───────────────────────────────────────────
            int rowIdx = 1;
            for (Map<String, Object> r : rows) {
                Row excelRow = sheet.createRow(rowIdx++);
                excelRow.createCell(0).setCellValue(str(r.get("drug_name_en")));
                excelRow.createCell(1).setCellValue(str(r.get("drug_name_cn")));
                // 靶点组合：优先 targets_raw，逗号 → " + " 展示
                String targetsRaw = str(r.get("targets_raw"));
                String targetsDisplay = targetsRaw != null
                        ? targetsRaw.replace(",", " + ").replace("，", " + ")
                        : "";
                excelRow.createCell(2).setCellValue(targetsDisplay);
                excelRow.createCell(3).setCellValue(str(r.get("phase")));
                Object score = r.get("phase_score");
                if (score instanceof Number n) {
                    Cell scoreCell = excelRow.createCell(4);
                    scoreCell.setCellValue(n.doubleValue());
                    scoreCell.setCellStyle(numStyle);
                } else {
                    excelRow.createCell(4).setCellValue(score != null ? score.toString() : "");
                }
                excelRow.createCell(5).setCellValue(str(r.get("originator")));
                excelRow.createCell(6).setCellValue(str(r.get("research_institute")));
                excelRow.createCell(7).setCellValue(str(r.get("phase_date")));
                excelRow.createCell(8).setCellValue(str(r.get("nct_id")));
                excelRow.createCell(9).setCellValue(str(r.get("disease_name")));
                excelRow.createCell(10).setCellValue(str(r.get("ta_name")));
                excelRow.createCell(11).setCellValue(str(r.get("is_combo")));
            }

            // ── 自适应列宽 ────────────────────────────────────────
            int[] colWidths = { 30, 20, 30, 14, 10, 20, 35, 14, 20, 35, 16, 10 };
            for (int i = 0; i < colWidths.length; i++) {
                sheet.setColumnWidth(i, colWidths[i] * 256);
            }

            // ── 冻结首行 ──────────────────────────────────────────
            sheet.createFreezePane(0, 1);

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
