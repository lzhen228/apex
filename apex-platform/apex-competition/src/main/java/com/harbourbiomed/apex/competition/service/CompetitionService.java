package com.harbourbiomed.apex.competition.service;

import com.harbourbiomed.apex.competition.request.MatrixQueryRequest;
import com.harbourbiomed.apex.competition.vo.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * 竞争格局服务接口
 *
 * @author Harbour BioMed
 */
public interface CompetitionService {

    /**
     * 获取疾病树形结构
     *
     * @return 治疗领域列表（包含关联疾病）
     */
    List<TherapeuticAreaVO> getDiseaseTree();

    /**
     * 查询竞争格局矩阵
     *
     * @param request 查询请求
     * @return 矩阵响应
     */
    MatrixResponse queryMatrix(MatrixQueryRequest request);

    /**
     * 获取指定靶点和疾病下的药物详情
     *
     * @param target 靶点名称
     * @param diseaseId 疾病 ID
     * @param phases 研发阶段（可选）
     * @return 药物响应
     */
    CellDrugsResponse getCellDrugs(String target, Integer diseaseId, List<String> phases);

    /**
     * 导出矩阵数据为 Excel 文件
     *
     * @param request 查询请求
     * @param response HTTP 响应
     * @throws IOException IO 异常
     */
    void exportMatrix(MatrixQueryRequest request, HttpServletResponse response) throws IOException;
}
