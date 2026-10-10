package kg.miras.backend.catalog.dal.repository;

import java.util.UUID;
import kg.miras.backend.catalog.dal.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, UUID> {
}
