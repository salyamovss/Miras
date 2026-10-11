package kg.miras.backend.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MaterialTranslationDraftDto(
        @NotBlank @Size(max = 2) String languageCode,
        @Size(max = 500) String title,
        String shortDescription
) {
}
