package kg.miras.backend.catalog.dal.repository;

import java.util.Optional;
import java.util.UUID;
import kg.miras.backend.catalog.dal.entity.Material;
import kg.miras.backend.catalog.dal.enums.MaterialStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, UUID> {
    Page<Material> findByStatus(MaterialStatus status, Pageable pageable);

    Optional<Material> findByIdAndStatus(UUID id, MaterialStatus status);
}
