package kg.miras.backend.catalog.dal.repository;

import java.util.UUID;
import kg.miras.backend.catalog.dal.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, UUID> {
}
