package kg.miras.backend.catalog.service.impl;

import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import kg.miras.backend.catalog.dal.entity.Author;
import kg.miras.backend.catalog.dal.entity.Material;
import kg.miras.backend.catalog.dal.entity.MaterialAuthor;
import kg.miras.backend.catalog.dal.entity.MaterialAuthorId;
import kg.miras.backend.catalog.dal.entity.MaterialTranslation;
import kg.miras.backend.catalog.dal.enums.MaterialStatus;
import kg.miras.backend.catalog.dal.repository.AuthorRepository;
import kg.miras.backend.catalog.dal.repository.MaterialRepository;
import kg.miras.backend.catalog.dto.request.MaterialDraftRequest;
import kg.miras.backend.catalog.dto.request.MaterialTranslationDraftDto;
import kg.miras.backend.catalog.dto.response.MaterialDraftResponse;
import kg.miras.backend.catalog.mapper.AuthorMapper;
import kg.miras.backend.catalog.mapper.MaterialMapper;
import kg.miras.backend.catalog.mapper.MaterialTranslationMapper;
import kg.miras.backend.catalog.service.MaterialService;
import kg.miras.backend.common.exception.BadRequestException;
import kg.miras.backend.common.exception.ConflictException;
import kg.miras.backend.common.exception.ForbiddenOperationException;
import kg.miras.backend.common.exception.ResourceNotFoundException;
import kg.miras.backend.identity.service.AdminUserService;
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
    private final MaterialTranslationMapper translationMapper;
    private final AdminUserService adminUserService;
    private final EntityManager entityManager;

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
        material.setStatus(MaterialStatus.DRAFT);
        // Add into the Lombok-initialized Set so Hibernate replaces it with a
        // PersistentSet on save (needed for orphanRemoval on later updates).
        material.getAuthors().addAll(mapAuthors(material, request.authors()));

        Material saved = materialRepository.save(material);
        return new MaterialDraftResponse(saved.getId());
    }

    @Override
    @Transactional
    public MaterialDraftResponse updateDraft(UUID materialId, UUID adminUserId, MaterialDraftRequest request) {
        if (materialId == null || adminUserId == null || request == null) {
            throw new BadRequestException("materialId, adminUserId, and request are required");
        }
        validateLanguage(request.primaryLanguageCode());
        validateTranslations(request);

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
        if (material.getStatus() != MaterialStatus.DRAFT) {
            throw new ConflictException("Only DRAFT materials can be edited");
        }
        if (!material.getCreatedById().equals(adminUserId)) {
            throw new ForbiddenOperationException("Only the author of the draft can edit it");
        }

        // Flush pending inserts from a prior save in the same transaction and
        // then re-fetch so the collections are proper PersistentSets that
        // honor orphanRemoval on clear().
        entityManager.flush();
        entityManager.refresh(material);

        material.setMaterialType(request.materialType());
        material.setPrimaryLanguageCode(normalizeOptionalLanguage(request.primaryLanguageCode()));
        material.setCountryId(request.countryId());
        material.setYearOrPeriod(request.yearOrPeriod());
        material.setCategoriesAndKeywords(request.categoriesAndKeywords());
        material.setCreationPlace(request.creationPlace());
        material.setAccessCondition(request.accessCondition());

        material.getTranslations().clear();
        material.getAuthors().clear();
        // Force the DELETEs to run before we add replacements with the same
        // (material_id, display_order) and (material_id, language_code) keys.
        entityManager.flush();

        if (request.translations() != null) {
            for (MaterialTranslationDraftDto dto : request.translations()) {
                MaterialTranslation translation = translationMapper.toEntity(dto);
                translation.setMaterial(material);
                material.getTranslations().add(translation);
            }
        }

        material.getAuthors().addAll(mapAuthors(material, request.authors()));

        return new MaterialDraftResponse(material.getId());
    }

    @Override
    @Transactional
    public void submitForReview(UUID materialId, UUID adminUserId) {
        if (materialId == null || adminUserId == null) {
            throw new BadRequestException("materialId and adminUserId are required");
        }
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
        if (material.getStatus() != MaterialStatus.DRAFT) {
            throw new ConflictException("Only DRAFT materials can be submitted for review");
        }
        if (!material.getCreatedById().equals(adminUserId)) {
            throw new ForbiddenOperationException("Only the author of the draft can submit it for review");
        }
        material.setStatus(MaterialStatus.PENDING_REVIEW);
    }

    @Override
    @Transactional
    public void archive(UUID materialId, UUID superAdminId) {
        adminUserService.verifySuperAdmin(superAdminId);
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
        if (material.getStatus() != MaterialStatus.PUBLISHED) {
            throw new ConflictException("Only PUBLISHED materials can be archived");
        }
        material.setStatus(MaterialStatus.ARCHIVED);
    }

    @Override
    @Transactional
    public void unarchive(UUID materialId, UUID superAdminId) {
        adminUserService.verifySuperAdmin(superAdminId);
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
        if (material.getStatus() != MaterialStatus.ARCHIVED) {
            throw new ConflictException("Only ARCHIVED materials can be unarchived");
        }
        material.setStatus(MaterialStatus.PUBLISHED);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        if (id == null) {
            return false;
        }
        return materialRepository.existsById(id);
    }

    @Override
    @Transactional
    public void publishMaterial(UUID materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
        material.setStatus(MaterialStatus.PUBLISHED);
        material.setPublishedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Override
    @Transactional
    public void returnToDraft(UUID materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
        material.setStatus(MaterialStatus.DRAFT);
    }

    @Override
    @Transactional(readOnly = true)
    public UUID getCreatedByIdOrThrow(UUID materialId) {
        return materialRepository.findById(materialId)
                .map(Material::getCreatedById)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
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
