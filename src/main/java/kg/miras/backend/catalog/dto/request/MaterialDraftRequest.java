package kg.miras.backend.catalog.dto.request;

import java.util.List;
import kg.miras.backend.catalog.dal.enums.MaterialAccessCondition;
import kg.miras.backend.catalog.dal.enums.MaterialType;

public record MaterialDraftRequest(
        MaterialType materialType,
        String primaryLanguageCode,
        String country,
        String yearOrPeriod,
        String categoriesAndKeywords,
        String creationPlace,
        MaterialAccessCondition accessCondition,
        List<MaterialTranslationDraftDto> translations,
        List<String> authors
) {
}
