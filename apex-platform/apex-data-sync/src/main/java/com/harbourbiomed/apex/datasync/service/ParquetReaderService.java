package com.harbourbiomed.apex.datasync.service;

import com.harbourbiomed.apex.datasync.entity.CiTrackingInfoEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.parquet.io.api.Binary;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Parquet 文件读取服务
 * 
 * 负责从 Parquet 文件中读取 CI 追踪信息
 * 
 * @author Harbour BioMed
 */
@Slf4j
@Service
public class ParquetReaderService {

    /**
     * 读取 Parquet 文件中的 CI 追踪信息
     * 
     * @param filePath Parquet 文件路径
     * @return CI 追踪信息列表
     */
    public List<CiTrackingInfoEntity> readCiTrackingInfo(String filePath) {
        log.info("开始读取 Parquet 文件: {}", filePath);
        List<CiTrackingInfoEntity> result = new ArrayList<>();

        try {
            File parquetFile = new File(filePath);
            if (!parquetFile.exists()) {
                throw new RuntimeException("Parquet 文件不存在: " + filePath);
            }

            Configuration conf = new Configuration();
            org.apache.hadoop.fs.Path parquetPath =
                    new org.apache.hadoop.fs.Path(parquetFile.getAbsolutePath());

            try (ParquetReader<Group> reader = ParquetReader.builder(new GroupReadSupport(), parquetPath)
                    .withConf(conf)
                    .build()) {
                Group group;
                while ((group = reader.read()) != null) {
                    CiTrackingInfoEntity entity = convertToEntity(group);
                    if (entity != null) {
                        result.add(entity);
                    }
                }

                log.info("成功读取 {} 条记录", result.size());
            }

        } catch (Exception e) {
            log.error("读取 Parquet 文件失败: {}", filePath, e);
            throw new RuntimeException("读取 Parquet 文件失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 将 Parquet Group 转换为 CiTrackingInfoEntity
     * 
     * @param group Parquet Group 对象
     * @return CI 追踪信息实体
     */
    private CiTrackingInfoEntity convertToEntity(Group group) {
        try {
            CiTrackingInfoEntity entity = new CiTrackingInfoEntity();

            // 设置基本字段
            entity.setDrugId(getStringValue(group, "drug_id"));
            entity.setDrugNameEn(getStringValue(group, "drug_name_en"));
            entity.setDrugNameCn(getStringValue(group, "drug_name_cn"));
            
            // 处理 targets 字段（可能是逗号分隔的字符串）
            String targetsRaw = getStringValue(group, "targets");
            entity.setTargetsRaw(targetsRaw);
            if (targetsRaw != null && !targetsRaw.isEmpty()) {
                String[] targets = targetsRaw.split(",");
                entity.setTargets(targets);
            }
            
            entity.setDiseaseId(getStringValue(group, "disease_id"));
            entity.setHarbourIndicationName(getStringValue(group, "harbour_indication_name"));
            entity.setTa(getStringValue(group, "ta"));
            entity.setMoa(getStringValue(group, "moa"));
            entity.setOriginator(getStringValue(group, "originator"));
            entity.setResearchInstitute(getStringValue(group, "research_institute"));
            
            entity.setGlobalHighestPhase(getStringValue(group, "global_highest_phase"));
            entity.setGlobalHighestPhaseScore(getIntValue(group, "global_highest_phase_score"));
            
            entity.setIndicationTopGlobalLatestStage(getStringValue(group, "indication_top_global_latest_stage"));
            entity.setIndicationTopGlobalStartDate(getStringValue(group, "indication_top_global_start_date"));
            
            entity.setHighestTrialId(getStringValue(group, "highest_trial_id"));
            entity.setHighestTrialPhase(getStringValue(group, "highest_trial_phase"));
            entity.setNctId(getStringValue(group, "nct_id"));
            entity.setDataSource(getStringValue(group, "data_source"));
            
            // 设置默认时间
            entity.setCreatedAt(LocalDateTime.now());

            return entity;

        } catch (Exception e) {
            log.error("转换 Group 到 Entity 失败", e);
            return null;
        }
    }

    /**
     * 从 Group 中获取字符串值
     * 
     * @param group Parquet Group
     * @param fieldName 字段名
     * @return 字符串值
     */
    private String getStringValue(Group group, String fieldName) {
        try {
            if (!group.getType().containsField(fieldName)) {
                return null;
            }
            
            int fieldCount = group.getFieldRepetitionCount(fieldName);
            if (fieldCount == 0) {
                return null;
            }
            
            Binary value = group.getBinary(fieldName, 0);
            if (value == null) {
                return null;
            }

            return value.toStringUsingUTF8();
            
        } catch (Exception e) {
            try {
                return group.getString(fieldName, 0);
            } catch (Exception ex) {
                log.debug("获取字段 {} 的值失败: {}", fieldName, ex.getMessage());
                return null;
            }
        }
    }

    /**
     * 从 Group 中获取整数值
     * 
     * @param group Parquet Group
     * @param fieldName 字段名
     * @return 整数值
     */
    private Integer getIntValue(Group group, String fieldName) {
        try {
            if (!group.getType().containsField(fieldName)) {
                return null;
            }
            
            int fieldCount = group.getFieldRepetitionCount(fieldName);
            if (fieldCount == 0) {
                return null;
            }
            
            return group.getInteger(fieldName, 0);
            
        } catch (Exception e) {
            try {
                return Integer.parseInt(group.getValueToString(group.getType().getFieldIndex(fieldName), 0));
            } catch (Exception ex) {
                log.debug("获取字段 {} 的整数值失败: {}", fieldName, ex.getMessage());
                return null;
            }
        }
    }
}
