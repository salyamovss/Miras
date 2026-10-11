package kg.miras.backend.notification.service.impl;

import java.util.UUID;
import kg.miras.backend.notification.dal.entity.Notification;
import kg.miras.backend.notification.dal.enums.NotificationType;
import kg.miras.backend.notification.dal.repository.NotificationRepository;
import kg.miras.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void notifyApproved(UUID recipientId, UUID materialId, UUID publicationRequestId) {
        save(recipientId, NotificationType.PUBLICATION_APPROVED, materialId, publicationRequestId);
    }

    @Override
    @Transactional
    public void notifyRejected(UUID recipientId, UUID materialId, UUID publicationRequestId) {
        save(recipientId, NotificationType.PUBLICATION_REJECTED, materialId, publicationRequestId);
    }

    private void save(UUID recipientId, NotificationType type, UUID materialId, UUID publicationRequestId) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setType(type);
        notification.setMaterialId(materialId);
        notification.setPublicationRequestId(publicationRequestId);
        notificationRepository.save(notification);
    }
}
