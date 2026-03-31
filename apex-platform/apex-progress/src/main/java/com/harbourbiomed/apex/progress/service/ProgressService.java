package com.harbourbiomed.apex.progress.service;

import com.harbourbiomed.apex.progress.dto.DiseaseViewRequest;
import com.harbourbiomed.apex.progress.dto.DiseaseViewResponse;
import com.harbourbiomed.apex.progress.dto.TargetListResponse;

/**
 * 进展服务接口
 *
 * @author Harbour BioMed
 */
public interface ProgressService {

    /**
     * 根据疾病 ID 查询靶点列表
     *
     * @param diseaseId 疾病 ID
     * @return 靶点列表响应
     */
    TargetListResponse getTargetsByDisease(Integer diseaseId);

    /**
     * 查询疾病研发进展视图
     *
     * @param request 疾病视图请求参数
     * @return 疾病视图响应
     */
    DiseaseViewResponse getDiseaseView(DiseaseViewRequest request);
}
