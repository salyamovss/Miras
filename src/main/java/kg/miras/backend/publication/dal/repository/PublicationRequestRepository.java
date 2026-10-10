package kg.miras.backend.publication.dal.repository;

import java.util.List;
import java.util.UUID;
import kg.miras.backend.publication.dal.entity.PublicationRequest;
import kg.miras.backend.publication.dal.enums.PublicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationRequestRepository extends JpaRepository<PublicationRequest, UUID> {
    List<PublicationRequest> findAllByStatusOrderBySubmittedAtAsc(PublicationStatus status);
    boolean existsByMaterialIdAndStatus(UUID materialId, PublicationStatus status);
}
