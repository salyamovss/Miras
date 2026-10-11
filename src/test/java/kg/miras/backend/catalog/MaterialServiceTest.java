package kg.miras.backend.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import kg.miras.backend.BaseIntegrationTest;
import kg.miras.backend.TestDataFactory;
import kg.miras.backend.catalog.dal.entity.Material;
import kg.miras.backend.catalog.dal.enums.MaterialAccessCondition;
import kg.miras.backend.catalog.dal.enums.MaterialStatus;
import kg.miras.backend.catalog.dal.enums.MaterialType;
import kg.miras.backend.catalog.dal.repository.MaterialRepository;
import kg.miras.backend.catalog.dto.request.MaterialDraftRequest;
import kg.miras.backend.catalog.dto.request.MaterialTranslationDraftDto;
import kg.miras.backend.catalog.dto.response.MaterialDraftResponse;
import kg.miras.backend.catalog.service.MaterialService;
import kg.miras.backend.common.exception.ConflictException;
import kg.miras.backend.common.exception.ForbiddenOperationException;
import kg.miras.backend.identity.dal.entity.AdminUser;
import kg.miras.backend.identity.dal.enums.AdminRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MaterialServiceTest extends BaseIntegrationTest {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private TestDataFactory testData;

    @Autowired
    private EntityManager entityManager;

    private MaterialDraftRequest sampleRequest() {
        return new MaterialDraftRequest(
                MaterialType.BOOK_RESEARCH,
                "ky",
                null,
                "19th century",
                "folklore,epic",
                "Osh region",
                MaterialAccessCondition.OPEN_ACCESS,
                List.of(new MaterialTranslationDraftDto("ky", "Манас", "Эпос")),
                List.of("Chokan Valikhanov")
        );
    }

    @Test
    void saveDraft_createsMaterialInDraftStatus() {
        AdminUser admin = testData.createAdmin(AdminRole.ADMIN);

        MaterialDraftResponse response = materialService.saveDraft(admin.getId(), sampleRequest());

        Material saved = materialRepository.findById(response.materialId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(MaterialStatus.DRAFT);
        assertThat(saved.getCreatedById()).isEqualTo(admin.getId());
        assertThat(saved.getMaterialType()).isEqualTo(MaterialType.BOOK_RESEARCH);
        assertThat(saved.getPrimaryLanguageCode()).isEqualTo("ky");
    }

    @Test
    void saveDraft_persistsTranslationsWithMaterialId() {
        AdminUser admin = testData.createAdmin(AdminRole.ADMIN);

        MaterialDraftResponse response = materialService.saveDraft(admin.getId(), sampleRequest());

        entityManager.flush();
        entityManager.clear();

        Material loaded = materialRepository.findById(response.materialId()).orElseThrow();
        assertThat(loaded.getTranslations()).hasSize(1);
        assertThat(loaded.getTranslations().iterator().next().getTitle()).isEqualTo("Манас");
        assertThat(loaded.getTranslations().iterator().next().getId().getMaterialId())
                .isEqualTo(response.materialId());
    }

    @Test
    void updateDraft_updatesFields() {
        AdminUser admin = testData.createAdmin(AdminRole.ADMIN);
        UUID materialId = materialService.saveDraft(admin.getId(), sampleRequest()).materialId();

        MaterialDraftRequest updated = new MaterialDraftRequest(
                MaterialType.AUDIO,
                "ru",
                null,
                "20th century",
                "music",
                "Bishkek",
                MaterialAccessCondition.READ_ONLY,
                List.of(new MaterialTranslationDraftDto("ru", "Новое", "Описание")),
                List.of("Author 1")
        );

        materialService.updateDraft(materialId, admin.getId(), updated);
        entityManager.flush();
        entityManager.clear();

        Material loaded = materialRepository.findById(materialId).orElseThrow();
        assertThat(loaded.getMaterialType()).isEqualTo(MaterialType.AUDIO);
        assertThat(loaded.getPrimaryLanguageCode()).isEqualTo("ru");
        assertThat(loaded.getYearOrPeriod()).isEqualTo("20th century");
        assertThat(loaded.getAccessCondition()).isEqualTo(MaterialAccessCondition.READ_ONLY);
    }

    @Test
    void updateDraft_rejectsIfNotDraft() {
        AdminUser admin = testData.createAdmin(AdminRole.ADMIN);
        Material material = testData.createMaterialWithStatus(admin.getId(), MaterialStatus.PENDING_REVIEW);

        assertThatThrownBy(() -> materialService.updateDraft(material.getId(), admin.getId(), sampleRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    void submitForReview_movesDraftToPendingReview() {
        AdminUser admin = testData.createAdmin(AdminRole.ADMIN);
        UUID materialId = materialService.saveDraft(admin.getId(), sampleRequest()).materialId();

        materialService.submitForReview(materialId, admin.getId());

        Material loaded = materialRepository.findById(materialId).orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(MaterialStatus.PENDING_REVIEW);
    }

    @Test
    void submitForReview_rejectsIfNotDraft() {
        AdminUser admin = testData.createAdmin(AdminRole.ADMIN);
        Material material = testData.createMaterialWithStatus(admin.getId(), MaterialStatus.PUBLISHED);

        assertThatThrownBy(() -> materialService.submitForReview(material.getId(), admin.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void archive_movesPublishedToArchived_whenSuperAdmin() {
        AdminUser admin = testData.createAdmin(AdminRole.ADMIN);
        AdminUser superAdmin = testData.createAdmin(AdminRole.SUPER_ADMIN);
        Material material = testData.createMaterialWithStatus(admin.getId(), MaterialStatus.PUBLISHED);

        materialService.archive(material.getId(), superAdmin.getId());

        Material loaded = materialRepository.findById(material.getId()).orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(MaterialStatus.ARCHIVED);
    }

    @Test
    void archive_rejectsNonSuperAdmin() {
        AdminUser admin = testData.createAdmin(AdminRole.ADMIN);
        Material material = testData.createMaterialWithStatus(admin.getId(), MaterialStatus.PUBLISHED);

        assertThatThrownBy(() -> materialService.archive(material.getId(), admin.getId()))
                .isInstanceOf(ForbiddenOperationException.class);
    }
}
