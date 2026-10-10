package kg.miras.backend.catalog.dal.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import kg.miras.backend.catalog.dal.enums.MaterialAccessCondition;
import kg.miras.backend.catalog.dal.enums.MaterialType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "material")
@Getter
@Setter
@NoArgsConstructor
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", length = 64)
    private MaterialType materialType;

    @Column(name = "primary_language_code", length = 2)
    private String primaryLanguageCode;

    @Column(length = 120)
    private String country;

    @Column(name = "year_or_period", length = 120)
    private String yearOrPeriod;

    @Column(name = "categories_and_keywords", columnDefinition = "text")
    private String categoriesAndKeywords;

    @Column(name = "creation_place", columnDefinition = "text")
    private String creationPlace;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_condition", length = 32)
    private MaterialAccessCondition accessCondition;

    @Column(name = "created_by", nullable = false)
    private UUID createdById;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MaterialTranslation> translations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MaterialAuthor> authors = new LinkedHashSet<>();
}
