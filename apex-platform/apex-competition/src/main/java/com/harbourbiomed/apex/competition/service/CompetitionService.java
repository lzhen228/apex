package com.harbourbiomed.apex.competition.service;

import com.harbourbiomed.apex.competition.request.MatrixQueryRequest;
import com.harbourbiomed.apex.competition.vo.CellDrugsResponse;
import com.harbourbiomed.apex.competition.vo.DiseaseTreeVO;
import com.harbourbiomed.apex.competition.vo.MatrixResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface CompetitionService {

    List<DiseaseTreeVO> getDiseaseTree();

    MatrixResponse queryMatrix(MatrixQueryRequest req);

    CellDrugsResponse getCellDrugs(String target, String pairTarget, List<Integer> diseaseIds, List<String> phases);

    void exportMatrix(MatrixQueryRequest req, HttpServletResponse response) throws IOException;
}
