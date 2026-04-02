package com.harbourbiomed.apex.progress.service;

import com.harbourbiomed.apex.progress.request.DiseaseViewRequest;
import com.harbourbiomed.apex.progress.vo.DiseaseViewResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ProgressService {

    List<Map<String, Object>> getTargetsByDisease(Integer diseaseId);

    DiseaseViewResponse getDiseaseView(DiseaseViewRequest req);

    void exportDiseaseView(DiseaseViewRequest req, HttpServletResponse response) throws IOException;
}
