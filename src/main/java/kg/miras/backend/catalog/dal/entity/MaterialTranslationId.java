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
public class MaterialTranslationId implements Serializable {
    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Column(name = "language_code", nullable = false, length = 2)
    private String languageCode;
}
