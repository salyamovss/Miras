package kg.miras.backend.media.dal.repository;

import java.util.List;
import java.util.UUID;
import kg.miras.backend.media.dal.entity.MaterialFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialFileRepository extends JpaRepository<MaterialFile, UUID> {
    List<MaterialFile> findAllByMaterialIdOrderByCreatedAtAsc(UUID materialId);
}
