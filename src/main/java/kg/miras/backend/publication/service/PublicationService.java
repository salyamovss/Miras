package kg.miras.backend.publication.service;

import java.util.UUID;

public interface PublicationService {
    UUID submit(UUID materialId, UUID adminUserId);

    void approve(UUID requestId, UUID superAdminId, String reviewNote);

    void reject(UUID requestId, UUID superAdminId, String reviewNote);
}
