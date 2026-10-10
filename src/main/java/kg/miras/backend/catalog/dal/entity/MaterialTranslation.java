package kg.miras.backend.catalog.dal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "material_translation")
@Getter
@Setter
@NoArgsConstructor
public class MaterialTranslation {
    @EmbeddedId
    private MaterialTranslationId id;

    @MapsId("materialId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(length = 500)
    private String title;

    @Column(name = "short_description", columnDefinition = "text")
    private String shortDescription;
}
