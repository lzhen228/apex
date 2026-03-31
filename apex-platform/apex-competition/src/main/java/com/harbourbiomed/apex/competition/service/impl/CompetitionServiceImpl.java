package com.harbourbiomed.apex.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.harbourbiomed.apex.competition.converter.DiseaseConverter;
import com.harbourbiomed.apex.competition.entity.CiTrackingInfo;
import com.harbourbiomed.apex.competition.entity.Disease;
import com.harbourbiomed.apex.competition.entity.TherapeuticArea;
import com.harbourbiomed.apex.competition.mapper.CompetitionCiTrackingInfoMapper;
import com.harbourbiomed.apex.competition.mapper.DiseaseMapper;
import com.harbourbiomed.apex.competition.mapper.TherapeuticAreaMapper;
import com.harbourbiomed.apex.competition.request.MatrixQueryRequest;
import com.harbourbiomed.apex.competition.service.CompetitionService;
import com.harbourbiomed.apex.competition.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 竞争格局服务实现类
 *
 * @author Harbour BioMed
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {

    private final TherapeuticAreaMapper therapeuticAreaMapper;
    private final DiseaseMapper diseaseMapper;
    private final CompetitionCiTrackingInfoMapper ciTrackingInfoMapper;
    private final DiseaseConverter diseaseConverter;

    @Override
    @Cacheable(value = "diseaseTree", key = "'all'")
    public List<TherapeuticAreaVO> getDiseaseTree() {
        log.info("查询疾病树形结构");

        // 查询所有治疗领域
        List<TherapeuticArea> areas = therapeuticAreaMapper.selectList(
                new LambdaQueryWrapper<TherapeuticArea>()
                        .orderByAsc(TherapeuticArea::getSortOrder)
        );

        // 查询所有疾病
        List<Disease> allDiseases = diseaseMapper.selectList(null);

        // 将疾病按治疗领域分组
        Map<Integer, List<Disease>> diseasesByArea = allDiseases.stream()
                .collect(Collectors.groupingBy(Disease::getTaId));

        // 组装树形结构
        List<TherapeuticAreaVO> result = new ArrayList<>();
        for (TherapeuticArea area : areas) {
            TherapeuticAreaVO vo = new TherapeuticAreaVO();
            vo.setId(area.getId());
            vo.setNameEn(area.getNameEn());
            vo.setNameCn(area.getNameCn());

            // 设置该治疗领域下的疾病列表
            List<Disease> diseases = diseasesByArea.getOrDefault(area.getId(), new ArrayList<>());
            vo.setDiseases(diseaseConverter.toDiseaseVOList(diseases));

            result.add(vo);
        }

        log.info("查询疾病树形结构完成，共 {} 个治疗领域", result.size());
        return result;
    }

    @Override
    public MatrixResponse queryMatrix(MatrixQueryRequest request) {
        log.info("查询竞争格局矩阵，请求参数：{}", request);

        MatrixResponse response = new MatrixResponse();

        // 1. 查询靶点聚合信息
        List<Map<String, Object>> targetAggregations = ciTrackingInfoMapper.queryTargetAggregation(request);

        // 2. 构建靶点行数据
        List<TargetRowVO> rows = buildTargetRows(targetAggregations, request);

        // 3. 构建疾病列数据
        List<DiseaseColumnVO> columns = buildDiseaseColumns(request);

        // 4. 构建汇总信息
        MatrixSummaryVO summary = buildSummary(rows, columns);

        response.setRows(rows);
        response.setColumns(columns);
        response.setSummary(summary);

        log.info("查询竞争格局矩阵完成，共 {} 个靶点，{} 个疾病", rows.size(), columns.size());
        return response;
    }

    @Override
    public CellDrugsResponse getCellDrugs(String target, Integer diseaseId, List<String> phases) {
        log.info("查询单元格药物，target={}, diseaseId={}, phases={}", target, diseaseId, phases);

        CellDrugsResponse response = new CellDrugsResponse();

        // 调用 Mapper 查询
        List<CellDrugVO> drugs = ciTrackingInfoMapper.queryCellDrugs(target, diseaseId, phases);

        response.setDrugs(drugs);

        log.info("查询单元格药物完成，共 {} 个药物", drugs.size());
        return response;
    }

    @Override
    public void exportMatrix(MatrixQueryRequest request, HttpServletResponse response) throws IOException {
        log.info("导出矩阵数据为 Excel，请求参数：{}", request);

        // 查询矩阵数据
        MatrixResponse matrixData = queryMatrix(request);

        // 创建 Excel 工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("竞争格局矩阵");

            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 写入表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("靶点");
            headerRow.getCell(0).setCellStyle(headerStyle);
            headerRow.createCell(1).setCellValue("药物数量");
            headerRow.getCell(1).setCellStyle(headerStyle);
            headerRow.createCell(2).setCellValue("平均阶段分值");
            headerRow.getCell(2).setCellStyle(headerStyle);

            // 写入疾病列
            int colIndex = 3;
            for (DiseaseColumnVO column : matrixData.getColumns()) {
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(column.getDiseaseNameCn() != null ? column.getDiseaseNameCn() : column.getDiseaseNameEn());
                cell.setCellStyle(headerStyle);
            }

            // 写入数据行
            int rowIndex = 1;
            for (TargetRowVO row : matrixData.getRows()) {
                Row dataRow = sheet.createRow(rowIndex++);

                // 写入靶点名称
                Cell targetCell = dataRow.createCell(0);
                targetCell.setCellValue(row.getTargetName());
                targetCell.setCellStyle(dataStyle);

                // 写入药物数量
                Cell countCell = dataRow.createCell(1);
                countCell.setCellValue(row.getDrugCount());
                countCell.setCellStyle(dataStyle);

                // 写入平均阶段分值
                Cell scoreCell = dataRow.createCell(2);
                scoreCell.setCellValue(row.getAvgPhaseScore());
                scoreCell.setCellStyle(dataStyle);

                // 疾病列数据留空（可以根据实际需求填充）
            }

            // 自动调整列宽
            for (int i = 0; i < colIndex; i++) {
                sheet.autoSizeColumn(i);
            }

            // 设置响应头
            String fileName = URLEncoder.encode("竞争格局矩阵.xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            // 写入响应
            workbook.write(response.getOutputStream());

            log.info("导出矩阵数据完成");
        }
    }

    /**
     * 构建靶点行数据
     */
    private List<TargetRowVO> buildTargetRows(List<Map<String, Object>> targetAggregations,
                                               MatrixQueryRequest request) {
        List<TargetRowVO> rows = new ArrayList<>();

        for (Map<String, Object> aggregation : targetAggregations) {
            String target = (String) aggregation.get("targetName");
            Integer drugCount = ((Number) aggregation.get("drugCount")).intValue();
            Double avgPhaseScore = ((BigDecimal) aggregation.get("avgPhaseScore")).doubleValue();

            TargetRowVO row = new TargetRowVO();
            row.setTargetName(target);
            row.setDrugCount(drugCount);
            row.setAvgPhaseScore(avgPhaseScore);

            // 查询该靶点下的药物详情
            MatrixQueryRequest targetRequest = new MatrixQueryRequest();
            targetRequest.setDiseaseIds(request.getDiseaseIds());
            targetRequest.setTargets(Collections.singletonList(target));
            targetRequest.setPhases(request.getPhases());
            targetRequest.setOrigins(request.getOrigins());
            targetRequest.setMoaKeywords(request.getMoaKeywords());

            List<CiTrackingInfo> drugs = ciTrackingInfoMapper.queryMatrixData(targetRequest);
            List<CellDrugVO> cellDrugs = convertToCellDrugVO(drugs);

            row.setDrugs(cellDrugs);
            rows.add(row);
        }

        return rows;
    }

    /**
     * 构建疾病列数据
     */
    private List<DiseaseColumnVO> buildDiseaseColumns(MatrixQueryRequest request) {
        List<DiseaseColumnVO> columns = new ArrayList<>();

        if (request.getDiseaseIds() != null && !request.getDiseaseIds().isEmpty()) {
            // 根据 ID 列表查询
            List<Disease> diseases = diseaseMapper.selectList(
                    new LambdaQueryWrapper<Disease>()
                            .in(Disease::getId, request.getDiseaseIds())
            );

            for (Disease disease : diseases) {
                DiseaseColumnVO column = new DiseaseColumnVO();
                column.setDiseaseId(disease.getId());
                column.setDiseaseNameEn(disease.getNameEn());
                column.setDiseaseNameCn(disease.getNameCn());
                columns.add(column);
            }
        } else {
            // 查询所有疾病
            List<Disease> diseases = diseaseMapper.selectList(null);
            for (Disease disease : diseases) {
                DiseaseColumnVO column = new DiseaseColumnVO();
                column.setDiseaseId(disease.getId());
                column.setDiseaseNameEn(disease.getNameEn());
                column.setDiseaseNameCn(disease.getNameCn());
                columns.add(column);
            }
        }

        return columns;
    }

    /**
     * 构建汇总信息
     */
    private MatrixSummaryVO buildSummary(List<TargetRowVO> rows, List<DiseaseColumnVO> columns) {
        MatrixSummaryVO summary = new MatrixSummaryVO();

        // 计算总药物数
        int totalDrugs = rows.stream()
                .mapToInt(TargetRowVO::getDrugCount)
                .sum();

        summary.setTotalDrugs(totalDrugs);
        summary.setTotalTargets(rows.size());
        summary.setTotalDiseases(columns.size());

        return summary;
    }

    /**
     * 将 CiTrackingInfo 转换为 CellDrugVO
     */
    private List<CellDrugVO> convertToCellDrugVO(List<CiTrackingInfo> entities) {
        List<CellDrugVO> vos = new ArrayList<>();

        for (CiTrackingInfo entity : entities) {
            CellDrugVO vo = new CellDrugVO();
            vo.setDrugId(entity.getDrugId());
            vo.setDrugNameEn(entity.getDrugNameEn());
            vo.setDrugNameCn(entity.getDrugNameCn());
            vo.setPhase(entity.getGlobalHighestPhase());
            if (entity.getGlobalHighestPhaseScore() != null) {
                vo.setPhaseScore(entity.getGlobalHighestPhaseScore().doubleValue());
            }
            vo.setOriginator(entity.getOriginator());
            vo.setMoa(entity.getMoa());
            vo.setNctId(entity.getNctId());
            if (entity.getTargets() != null) {
                vo.setTargets(Arrays.asList(entity.getTargets()));
            }
            vo.setIndicationStartDate(entity.getIndicationTopGlobalStartDate());

            vos.add(vo);
        }

        return vos;
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
