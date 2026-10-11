package kg.miras.backend.catalog.dal.repository;

import java.util.Optional;
import java.util.UUID;
import kg.miras.backend.catalog.dal.entity.Country;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, UUID> {
    Optional<Country> findByCode(String code);

    Page<Country> findByActiveTrue(Pageable pageable);
}
