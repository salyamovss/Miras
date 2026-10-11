package kg.miras.backend.catalog.dal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class CountryTranslationId implements Serializable {
    @Column(name = "country_id", nullable = false)
    private UUID countryId;

    @Column(name = "language_code", nullable = false, length = 2)
    private String languageCode;
}
