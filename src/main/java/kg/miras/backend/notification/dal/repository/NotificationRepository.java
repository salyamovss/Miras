package kg.miras.backend.notification.dal.repository;

import java.util.UUID;
import kg.miras.backend.notification.dal.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    int countByRecipientIdAndReadAtIsNull(UUID recipientId);
}
