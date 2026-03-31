package com.harbourbiomed.apex.competition.converter;

import com.harbourbiomed.apex.competition.entity.Disease;
import com.harbourbiomed.apex.competition.entity.TherapeuticArea;
import com.harbourbiomed.apex.competition.vo.DiseaseVO;
import com.harbourbiomed.apex.competition.vo.TherapeuticAreaVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * 疾病相关实体转换器
 *
 * @author Harbour BioMed
 */
@Mapper(componentModel = "spring")
public interface DiseaseConverter {

    /**
     * 将治疗领域实体转换为 VO
     */
    @Mapping(target = "diseases", source = "diseases", qualifiedByName = "mapDiseases")
    TherapeuticAreaVO toVO(TherapeuticArea entity);

    /**
     * 将疾病实体列表转换为 VO 列表
     */
    List<DiseaseVO> toDiseaseVOList(List<Disease> diseases);

    /**
     * 映射疾病列表
     */
    @Named("mapDiseases")
    default List<DiseaseVO> mapDiseases(List<Disease> diseases) {
        return toDiseaseVOList(diseases);
    }
}
