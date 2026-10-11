package kg.miras.backend.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import kg.miras.backend.BaseIntegrationTest;
import kg.miras.backend.TestDataFactory;
import kg.miras.backend.catalog.dal.entity.Material;
import kg.miras.backend.catalog.dal.enums.MaterialStatus;
import kg.miras.backend.catalog.dal.repository.MaterialRepository;
import kg.miras.backend.common.exception.ConflictException;
import kg.miras.backend.common.exception.ForbiddenOperationException;
import kg.miras.backend.identity.dal.entity.AdminUser;
import kg.miras.backend.identity.dal.enums.AdminRole;
import kg.miras.backend.notification.dal.entity.Notification;
import kg.miras.backend.notification.dal.enums.NotificationType;
import kg.miras.backend.notification.dal.repository.NotificationRepository;
import kg.miras.backend.publication.dal.entity.PublicationRequest;
import kg.miras.backend.publication.dal.enums.PublicationStatus;
import kg.miras.backend.publication.dal.repository.PublicationRequestRepository;
import kg.miras.backend.publication.service.PublicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PublicationServiceTest extends BaseIntegrationTest {

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private PublicationRequestRepository publicationRequestRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TestDataFactory testData;

    @Autowired
    private EntityManager entityManager;

    @Test
    void submit_createsPendingRequest() {
        AdminUser admin = testData.createAdmin(AdminRole.ADMIN);
        Material material = testData.createDraftMaterial(admin.getId());

        UUID requestId = publicationService.submit(material.getId(), admin.getId());

        PublicationRequest request = publicationRequestRepository.findById(requestId).orElseThrow();
        assertThat(request.getStatus()).isEqualTo(PublicationStatus.PENDING_REVIEW);
        assertThat(request.getMaterialId()).isEqualTo(material.getId());
        assertThat(request.getSubmittedById()).isEqualTo(admin.getId());

        Material reloaded = materialRepository.findById(material.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(MaterialStatus.PENDING_REVIEW);
    }

    @Test
    void approve_movesRequestToApprovedAndMaterialToPublishedAndNotifies() {
        AdminUser admin = testData.createAdmin(AdminRole.ADMIN);
        AdminUser superAdmin = testData.createAdmin(AdminRole.SUPER_ADMIN);
        Material material = testData.createDraftMaterial(admin.getId());
        UUID requestId = publicationService.submit(material.getId(), admin.getId());

        publicationService.approve(requestId, superAdmin.getId(), "LGTM");
        entityManager.flush();
        entityManager.clear();

        PublicationRequest request = publicationRequestRepository.findById(requestId).orElseThrow();
        assertThat(request.getStatus()).isEqualTo(PublicationStatus.APPROVED);
        assertThat(request.getReviewedById()).isEqualTo(superAdmin.getId());
        assertThat(request.getReviewNote()).isEqualTo("LGTM");

        Material reloaded = materialRepository.findById(material.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(MaterialStatus.PUBLISHED);
        assertThat(reloaded.getPublishedAt()).isNotNull();

        Notification notification = notificationRepository.findAll().stream()
                .filter(n -> n.getPublicationRequestId() != null && n.getPublicationRequestId().equals(requestId))
                .findFirst()
                .orElseThrow();
        assertThat(notification.getType()).isEqualTo(NotificationType.PUBLICATION_APPROVED);
        assertThat(notification.getRecipientId()).isEqualTo(admin.getId());
        assertThat(notification.getMaterialId()).isEqualTo(material.getId());
    }

    @Test
    void reject_movesRequestToRejectedAndMaterialBackToDraftAndNotifies() {
        AdminUser admin = testData.createAdmin(AdminRole.ADMIN);
        AdminUser superAdmin = testData.createAdmin(AdminRole.SUPER_ADMIN);
        Material material = testData.createDraftMaterial(admin.getId());
        UUID requestId = publicationService.submit(material.getId(), admin.getId());

        publicationService.reject(requestId, superAdmin.getId(), "Needs more sources");
        entityManager.flush();
        entityManager.clear();

        PublicationRequest request = publicationRequestRepository.findById(requestId).orElseThrow();
        assertThat(request.getStatus()).isEqualTo(PublicationStatus.REJECTED);
        assertThat(request.getReviewNote()).isEqualTo("Needs more sources");

        Material reloaded = materialRepository.findById(material.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(MaterialStatus.DRAFT);

        Notification notification = notificationRepository.findAll().stream()
                .filter(n -> n.getPublicationRequestId() != null && n.getPublicationRequestId().equals(requestId))
                .findFirst()
                .orElseThrow();
        assertThat(notification.getType()).isEqualTo(NotificationType.PUBLICATION_REJECTED);
        assertThat(notification.getRecipientId()).isEqualTo(admin.getId());
    }

    @Test
    void approve_rejectsIfNotPending() {
        AdminUser admin = testData.createAdmin(AdminRole.ADMIN);
        AdminUser superAdmin = testData.createAdmin(AdminRole.SUPER_ADMIN);
        Material material = testData.createDraftMaterial(admin.getId());
        UUID requestId = publicationService.submit(material.getId(), admin.getId());
        publicationService.approve(requestId, superAdmin.getId(), null);

        assertThatThrownBy(() -> publicationService.approve(requestId, superAdmin.getId(), null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void approve_rejectsNonSuperAdmin() {
        AdminUser admin = testData.createAdmin(AdminRole.ADMIN);
        AdminUser otherAdmin = testData.createAdmin(AdminRole.ADMIN);
        Material material = testData.createDraftMaterial(admin.getId());
        UUID requestId = publicationService.submit(material.getId(), admin.getId());

        assertThatThrownBy(() -> publicationService.approve(requestId, otherAdmin.getId(), null))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void resubmitAfterRejection_createsNewRequest() {
        AdminUser admin = testData.createAdmin(AdminRole.ADMIN);
        AdminUser superAdmin = testData.createAdmin(AdminRole.SUPER_ADMIN);
        Material material = testData.createDraftMaterial(admin.getId());
        UUID firstRequestId = publicationService.submit(material.getId(), admin.getId());
        publicationService.reject(firstRequestId, superAdmin.getId(), "try again");

        UUID secondRequestId = publicationService.submit(material.getId(), admin.getId());

        assertThat(secondRequestId).isNotEqualTo(firstRequestId);
        PublicationRequest first = publicationRequestRepository.findById(firstRequestId).orElseThrow();
        PublicationRequest second = publicationRequestRepository.findById(secondRequestId).orElseThrow();
        assertThat(first.getStatus()).isEqualTo(PublicationStatus.REJECTED);
        assertThat(second.getStatus()).isEqualTo(PublicationStatus.PENDING_REVIEW);
    }
}
