package com.harbourbiomed.apex.progress.service.impl;

import com.harbourbiomed.apex.progress.dto.*;
import com.harbourbiomed.apex.progress.mapper.ProgressCiTrackingInfoMapper;
import com.harbourbiomed.apex.progress.service.ProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 进展服务实现类
 *
 * @author Harbour BioMed
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final ProgressCiTrackingInfoMapper progressCiTrackingInfoMapper;

    /**
     * 根据疾病 ID 查询靶点列表
     *
     * @param diseaseId 疾病 ID
     * @return 靶点列表响应
     */
    @Override
    @Cacheable(value = "targetList", key = "#diseaseId")
    public TargetListResponse getTargetsByDisease(Integer diseaseId) {
        log.info("查询疾病 ID {} 的靶点列表", diseaseId);

        List<TargetStatVO> targets = progressCiTrackingInfoMapper.selectTargetsByDisease(diseaseId);

        TargetListResponse response = new TargetListResponse();
        response.setTargets(targets);

        log.info("疾病 ID {} 查询到 {} 个靶点", diseaseId, targets.size());

        return response;
    }

    /**
     * 查询疾病研发进展视图
     *
     * @param request 疾病视图请求参数
     * @return 疾病视图响应
     */
    @Override
    public DiseaseViewResponse getDiseaseView(DiseaseViewRequest request) {
        log.info("查询疾病研发进展视图，疾病 ID: {}", request.getDiseaseId());

        // 查询药物管线数据
        List<ProgressDrugRecord> drugList = progressCiTrackingInfoMapper.selectDiseaseView(
                request.getDiseaseId(),
                request.getTargets(),
                request.getPhases(),
                request.getOrigins(),
                request.getSortBy(),
                request.getSortOrder()
        );

        // 构建疾病信息
        DiseaseInfoVO diseaseInfo = buildDiseaseInfo(drugList);

        // 按靶点分组并构建管线数据
        List<TargetPipeline> pipelineMap = groupByTargets(drugList);

        // 排序
        sortPipelines(pipelineMap, request.getSortBy(), request.getSortOrder());

        // 转换为 VO
        List<TargetPipelineVO> pipelineVOs = pipelineMap.stream()
                .map(this::convertToTargetPipelineVO)
                .collect(Collectors.toList());

        // 构建响应
        DiseaseViewResponse response = new DiseaseViewResponse();
        response.setDisease(diseaseInfo);
        response.setPipelines(pipelineVOs);

        log.info("疾病研发进展视图查询完成，返回 {} 个靶点管线", pipelineVOs.size());

        return response;
    }

    /**
     * 构建疾病信息
     */
    private DiseaseInfoVO buildDiseaseInfo(List<ProgressDrugRecord> drugList) {
        if (drugList == null || drugList.isEmpty()) {
            return null;
        }

        ProgressDrugRecord firstDrug = drugList.get(0);
        DiseaseInfoVO diseaseInfo = new DiseaseInfoVO();
        diseaseInfo.setId(firstDrug.getDiseaseId());
        diseaseInfo.setNameEn(firstDrug.getDiseaseNameEn());
        diseaseInfo.setNameCn(firstDrug.getDiseaseNameCn());
        diseaseInfo.setTaId(firstDrug.getTaId());

        return diseaseInfo;
    }

    /**
     * 按靶点分组
     */
    private List<TargetPipeline> groupByTargets(List<ProgressDrugRecord> drugList) {
        Map<String, TargetPipeline> pipelineMap = new LinkedHashMap<>();

        for (ProgressDrugRecord drug : drugList) {
            String[] targets = drug.getTargets();
            if (targets == null || targets.length == 0) {
                continue;
            }

            for (String target : targets) {
                TargetPipeline pipeline = pipelineMap.computeIfAbsent(target, k -> {
                    TargetPipeline p = new TargetPipeline();
                    p.setTargetName(target);
                    p.setDrugCount(0);
                    p.setTotalPhaseScore(0.0);
                    p.setDrugs(new ArrayList<>());
                    return p;
                });

                pipeline.getDrugs().add(drug);
                pipeline.setDrugCount(pipeline.getDrugCount() + 1);
                pipeline.setTotalPhaseScore(
                        pipeline.getTotalPhaseScore() + safeDouble(drug.getGlobalHighestPhaseScore())
                );
            }
        }

        // 计算平均分值
        for (TargetPipeline pipeline : pipelineMap.values()) {
            if (pipeline.getDrugCount() > 0) {
                pipeline.setAvgPhaseScore(pipeline.getTotalPhaseScore() / pipeline.getDrugCount());
            }
        }

        return new ArrayList<>(pipelineMap.values());
    }

    /**
     * 排序管线数据
     */
    private void sortPipelines(List<TargetPipeline> pipelines, String sortBy, String sortOrder) {
        Comparator<TargetPipeline> comparator;

        if ("targetName".equals(sortBy)) {
            comparator = Comparator.comparing(TargetPipeline::getTargetName);
        } else if ("drugCount".equals(sortBy)) {
            comparator = Comparator.comparing(TargetPipeline::getDrugCount);
        } else if ("avgPhaseScore".equals(sortBy)) {
            comparator = Comparator.comparing(TargetPipeline::getAvgPhaseScore);
        } else {
            // 默认按药物数量排序
            comparator = Comparator.comparing(TargetPipeline::getDrugCount);
        }

        boolean isDesc = !"ASC".equalsIgnoreCase(sortOrder);
        if (isDesc) {
            comparator = comparator.reversed();
        }

        pipelines.sort(comparator);
    }

    /**
     * 转换为 TargetPipelineVO
     */
    private TargetPipelineVO convertToTargetPipelineVO(TargetPipeline pipeline) {
        TargetPipelineVO vo = new TargetPipelineVO();
        vo.setTargetName(pipeline.getTargetName());
        vo.setDrugCount(pipeline.getDrugCount());
        vo.setAvgPhaseScore(pipeline.getAvgPhaseScore());

        List<DrugCardVO> drugCardVOs = pipeline.getDrugs().stream()
                .map(this::convertToDrugCardVO)
                .collect(Collectors.toList());
        vo.setDrugs(drugCardVOs);

        return vo;
    }

    /**
     * 转换为 DrugCardVO
     */
    private DrugCardVO convertToDrugCardVO(ProgressDrugRecord entity) {
        DrugCardVO vo = new DrugCardVO();
        vo.setDrugId(entity.getDrugId());
        vo.setDrugNameEn(entity.getDrugNameEn());
        vo.setDrugNameCn(entity.getDrugNameCn());
        vo.setGlobalHighestPhase(entity.getGlobalHighestPhase());
        vo.setPhaseScore(safeDouble(entity.getGlobalHighestPhaseScore()));
        vo.setOriginator(entity.getOriginator());
        vo.setMoa(entity.getMoa());
        vo.setNctId(entity.getNctId());
        vo.setIndicationTopStage(entity.getIndicationTopGlobalLatestStage());
        vo.setIndicationStartDate(parseDate(entity.getIndicationTopGlobalStartDate()));
        vo.setIndicationEndDate(null);
        vo.setTargets(entity.getTargets() == null ? Collections.emptyList() : Arrays.asList(entity.getTargets()));
        vo.setResearchInstitute(entity.getResearchInstitute());

        return vo;
    }

    private Double safeDouble(java.math.BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private Date parseDate(java.time.LocalDate value) {
        if (value == null) {
            return null;
        }
        return Date.valueOf(value);
    }

    /**
     * 内部使用的靶点管线数据结构
     */
    private static class TargetPipeline {
        private String targetName;
        private Integer drugCount;
        private Double totalPhaseScore;
        private Double avgPhaseScore;
        private List<ProgressDrugRecord> drugs;

        public String getTargetName() {
            return targetName;
        }

        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }

        public Integer getDrugCount() {
            return drugCount;
        }

        public void setDrugCount(Integer drugCount) {
            this.drugCount = drugCount;
        }

        public Double getTotalPhaseScore() {
            return totalPhaseScore;
        }

        public void setTotalPhaseScore(Double totalPhaseScore) {
            this.totalPhaseScore = totalPhaseScore;
        }

        public Double getAvgPhaseScore() {
            return avgPhaseScore;
        }

        public void setAvgPhaseScore(Double avgPhaseScore) {
            this.avgPhaseScore = avgPhaseScore;
        }

        public List<ProgressDrugRecord> getDrugs() {
            return drugs;
        }

        public void setDrugs(List<ProgressDrugRecord> drugs) {
            this.drugs = drugs;
        }
    }
}
