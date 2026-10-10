package kg.miras.backend.identity.dal.repository;

import java.util.Optional;
import java.util.UUID;
import kg.miras.backend.identity.dal.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {
    Optional<AdminUser> findByEmailIgnoreCase(String email);
}
