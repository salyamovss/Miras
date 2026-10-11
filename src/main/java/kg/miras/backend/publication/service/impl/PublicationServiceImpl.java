package kg.miras.backend.publication.service.impl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import kg.miras.backend.catalog.service.MaterialService;
import kg.miras.backend.common.exception.BadRequestException;
import kg.miras.backend.common.exception.ConflictException;
import kg.miras.backend.common.exception.ResourceNotFoundException;
import kg.miras.backend.identity.service.AdminUserService;
import kg.miras.backend.notification.service.NotificationService;
import kg.miras.backend.publication.dal.entity.PublicationRequest;
import kg.miras.backend.publication.dal.enums.PublicationStatus;
import kg.miras.backend.publication.dal.repository.PublicationRequestRepository;
import kg.miras.backend.publication.service.PublicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublicationServiceImpl implements PublicationService {
    private final PublicationRequestRepository publicationRequestRepository;
    private final MaterialService materialService;
    private final NotificationService notificationService;
    private final AdminUserService adminUserService;

    @Override
    @Transactional
    public UUID submit(UUID materialId, UUID adminUserId) {
        if (materialId == null || adminUserId == null) {
            throw new BadRequestException("Material and submitting admin IDs are required");
        }
        adminUserService.verifyExists(adminUserId);
        if (!materialService.existsById(materialId)) {
            throw new ResourceNotFoundException("Material not found");
        }
        if (publicationRequestRepository.existsByMaterialIdAndStatus(
                materialId, PublicationStatus.PENDING_REVIEW)) {
            throw new ConflictException("This material already has a pending publication request");
        }

        materialService.submitForReview(materialId, adminUserId);

        PublicationRequest request = new PublicationRequest();
        request.setMaterialId(materialId);
        request.setSubmittedById(adminUserId);
        request.setStatus(PublicationStatus.PENDING_REVIEW);
        return publicationRequestRepository.save(request).getId();
    }

    @Override
    @Transactional
    public void approve(UUID requestId, UUID superAdminId, String reviewNote) {
        adminUserService.verifySuperAdmin(superAdminId);
        PublicationRequest request = findPendingRequest(requestId);

        request.setStatus(PublicationStatus.APPROVED);
        request.setReviewedById(superAdminId);
        request.setReviewedAt(OffsetDateTime.now(ZoneOffset.UTC));
        request.setReviewNote(reviewNote);

        UUID createdBy = materialService.getCreatedByIdOrThrow(request.getMaterialId());
        materialService.publishMaterial(request.getMaterialId());
        notificationService.notifyApproved(createdBy, request.getMaterialId(), request.getId());
    }

    @Override
    @Transactional
    public void reject(UUID requestId, UUID superAdminId, String reviewNote) {
        adminUserService.verifySuperAdmin(superAdminId);
        PublicationRequest request = findPendingRequest(requestId);

        request.setStatus(PublicationStatus.REJECTED);
        request.setReviewedById(superAdminId);
        request.setReviewedAt(OffsetDateTime.now(ZoneOffset.UTC));
        request.setReviewNote(reviewNote);

        UUID createdBy = materialService.getCreatedByIdOrThrow(request.getMaterialId());
        materialService.returnToDraft(request.getMaterialId());
        notificationService.notifyRejected(createdBy, request.getMaterialId(), request.getId());
    }

    private PublicationRequest findPendingRequest(UUID requestId) {
        if (requestId == null) {
            throw new BadRequestException("requestId is required");
        }
        PublicationRequest request = publicationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Publication request was not found"));
        if (request.getStatus() != PublicationStatus.PENDING_REVIEW) {
            throw new ConflictException("Only a pending request can be decided");
        }
        return request;
    }
}
