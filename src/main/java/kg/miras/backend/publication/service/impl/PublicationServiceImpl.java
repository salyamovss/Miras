package kg.miras.backend.publication.service.impl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import kg.miras.backend.publication.dal.entity.PublicationRequest;
import kg.miras.backend.publication.dal.enums.PublicationStatus;
import kg.miras.backend.publication.dal.repository.PublicationRequestRepository;
import kg.miras.backend.publication.service.PublicationService;
import kg.miras.backend.common.exception.BadRequestException;
import kg.miras.backend.common.exception.ConflictException;
import kg.miras.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublicationServiceImpl implements PublicationService {
    private final PublicationRequestRepository publicationRequestRepository;

    @Override
    @Transactional
    public UUID submit(UUID materialId, UUID adminUserId) {
        if (materialId == null || adminUserId == null) {
            throw new BadRequestException("Material and submitting admin IDs are required");
        }
        if (publicationRequestRepository.existsByMaterialIdAndStatus(
                materialId, PublicationStatus.PENDING_REVIEW)) {
            throw new ConflictException("This material already has a pending publication request");
        }
        if (publicationRequestRepository.existsByMaterialIdAndStatus(
                materialId, PublicationStatus.APPROVED)) {
            throw new ConflictException("An approved material cannot be submitted again");
        }

        PublicationRequest request = new PublicationRequest();
        request.setMaterialId(materialId);
        request.setSubmittedById(adminUserId);
        request.setStatus(PublicationStatus.PENDING_REVIEW);
        return publicationRequestRepository.save(request).getId();
    }

    @Override
    @Transactional
    public void approve(UUID requestId, UUID superAdminId, String reviewNote) {
        decide(requestId, superAdminId, reviewNote, PublicationStatus.APPROVED);
    }

    @Override
    @Transactional
    public void reject(UUID requestId, UUID superAdminId, String reviewNote) {
        decide(requestId, superAdminId, reviewNote, PublicationStatus.REJECTED);
    }

    private void decide(UUID requestId, UUID superAdminId, String reviewNote, PublicationStatus decision) {
        if (superAdminId == null) {
            throw new BadRequestException("Reviewing Super Admin ID is required");
        }

        PublicationRequest request = publicationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Publication request was not found"));
        if (request.getStatus() != PublicationStatus.PENDING_REVIEW) {
            throw new ConflictException("Only a pending request can be decided");
        }

        request.setStatus(decision);
        request.setReviewedById(superAdminId);
        request.setReviewedAt(OffsetDateTime.now(ZoneOffset.UTC));
        request.setReviewNote(reviewNote);
    }
}
