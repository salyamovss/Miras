package kg.miras.backend.catalog.service.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import kg.miras.backend.catalog.dal.entity.Author;
import kg.miras.backend.catalog.dal.entity.Material;
import kg.miras.backend.catalog.dal.entity.MaterialAuthor;
import kg.miras.backend.catalog.dal.entity.MaterialAuthorId;
import kg.miras.backend.catalog.dal.repository.AuthorRepository;
import kg.miras.backend.catalog.dal.repository.MaterialRepository;
import kg.miras.backend.catalog.dto.request.MaterialDraftRequest;
import kg.miras.backend.catalog.dto.response.MaterialDraftResponse;
import kg.miras.backend.catalog.mapper.AuthorMapper;
import kg.miras.backend.catalog.mapper.MaterialMapper;
import kg.miras.backend.catalog.service.MaterialService;
import kg.miras.backend.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("ky", "ru", "en");

    private final MaterialRepository materialRepository;
    private final AuthorRepository authorRepository;
    private final MaterialMapper materialMapper;
    private final AuthorMapper authorMapper;

    @Override
    @Transactional
    public MaterialDraftResponse saveDraft(UUID adminUserId, MaterialDraftRequest request) {
        if (adminUserId == null) {
            throw new BadRequestException("adminUserId is required");
        }
        if (request == null) {
            throw new BadRequestException("Draft request is required");
        }

        validateLanguage(request.primaryLanguageCode());
        validateTranslations(request);

        Material material = materialMapper.toEntity(request);
        material.setCreatedById(adminUserId);
        material.setPrimaryLanguageCode(normalizeOptionalLanguage(request.primaryLanguageCode()));
        material.setAuthors(mapAuthors(material, request.authors()));

        Material saved = materialRepository.save(material);
        return new MaterialDraftResponse(saved.getId());
    }

    private Set<MaterialAuthor> mapAuthors(Material material, List<String> authorNames) {
        Set<MaterialAuthor> materialAuthors = new LinkedHashSet<>();
        if (authorNames == null || authorNames.isEmpty()) {
            return materialAuthors;
        }

        List<String> normalizedNames = authorNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .toList();
        if (normalizedNames.isEmpty()) {
            return materialAuthors;
        }

        List<Author> savedAuthors = authorRepository.saveAll(authorMapper.toEntities(normalizedNames));
        for (int index = 0; index < savedAuthors.size(); index++) {
            Author author = savedAuthors.get(index);
            MaterialAuthor link = new MaterialAuthor();
            MaterialAuthorId id = new MaterialAuthorId();
            id.setAuthorId(author.getId());
            link.setId(id);
            link.setMaterial(material);
            link.setAuthor(author);
            link.setDisplayOrder(index);
            materialAuthors.add(link);
        }
        return materialAuthors;
    }

    private void validateTranslations(MaterialDraftRequest request) {
        if (request.translations() == null) {
            return;
        }
        Set<String> seenLanguages = new LinkedHashSet<>();
        for (var translation : request.translations()) {
            if (translation == null) {
                throw new BadRequestException("Translation entries cannot be null");
            }
            String languageCode = normalizeOptionalLanguage(translation.languageCode());
            if (languageCode == null) {
                throw new BadRequestException("Each translation needs a language code");
            }
            validateLanguage(languageCode);
            if (!seenLanguages.add(languageCode)) {
                throw new BadRequestException("Only one translation per language is allowed");
            }
        }
    }

    private String normalizeOptionalLanguage(String languageCode) {
        return languageCode == null || languageCode.isBlank()
                ? null
                : languageCode.trim().toLowerCase(Locale.ROOT);
    }

    private void validateLanguage(String languageCode) {
        String normalized = normalizeOptionalLanguage(languageCode);
        if (normalized != null && !SUPPORTED_LANGUAGES.contains(normalized)) {
            throw new BadRequestException("Supported languages are ky, ru, and en");
        }
    }
}
