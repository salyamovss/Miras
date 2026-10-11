package kg.miras.backend.catalog.service;

import java.util.UUID;
import kg.miras.backend.catalog.dto.request.MaterialDraftRequest;
import kg.miras.backend.catalog.dto.response.MaterialDraftResponse;

public interface MaterialService {
    MaterialDraftResponse saveDraft(UUID adminUserId, MaterialDraftRequest request);

    MaterialDraftResponse updateDraft(UUID materialId, UUID adminUserId, MaterialDraftRequest request);

    void submitForReview(UUID materialId, UUID adminUserId);

    void archive(UUID materialId, UUID superAdminId);

    void unarchive(UUID materialId, UUID superAdminId);

    boolean existsById(UUID id);

    /** Internal transition used by {@code PublicationService} after approval. */
    void publishMaterial(UUID materialId);

    /** Internal transition used by {@code PublicationService} after rejection. */
    void returnToDraft(UUID materialId);

    /** Internal lookup used by {@code PublicationService} to identify the draft author. */
    UUID getCreatedByIdOrThrow(UUID materialId);
}
