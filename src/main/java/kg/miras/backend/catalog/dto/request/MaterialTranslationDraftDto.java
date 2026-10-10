package kg.miras.backend.catalog.dto.request;

public record MaterialTranslationDraftDto(
        String languageCode,
        String title,
        String shortDescription
) {
}
