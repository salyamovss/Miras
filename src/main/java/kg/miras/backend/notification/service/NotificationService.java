package kg.miras.backend.notification.service;

import java.util.UUID;

public interface NotificationService {
    void notifyApproved(UUID recipientId, UUID materialId, UUID publicationRequestId);

    void notifyRejected(UUID recipientId, UUID materialId, UUID publicationRequestId);
}
