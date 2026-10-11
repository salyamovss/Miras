package kg.miras.backend.catalog.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import kg.miras.backend.catalog.dal.enums.MaterialAccessCondition;
import kg.miras.backend.catalog.dal.enums.MaterialType;

public record MaterialDraftRequest(
        MaterialType materialType,
        @Size(max = 2) String primaryLanguageCode,
        UUID countryId,
        @Size(max = 120) String yearOrPeriod,
        String categoriesAndKeywords,
        String creationPlace,
        MaterialAccessCondition accessCondition,
        @Valid List<MaterialTranslationDraftDto> translations,
        @Size(max = 50) List<String> authors
) {
}
