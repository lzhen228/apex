package com.harbourbiomed.apex.competition.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 单元格药物响应对象
 *
 * @author Harbour BioMed
 */
@Data
@Schema(description = "单元格药物响应")
public class CellDrugsResponse {

    @Schema(description = "药物列表")
    private List<CellDrugVO> drugs;
}
