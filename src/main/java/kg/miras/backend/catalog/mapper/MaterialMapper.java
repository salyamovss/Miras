package kg.miras.backend.catalog.mapper;

import kg.miras.backend.catalog.dal.entity.Material;
import kg.miras.backend.catalog.dal.entity.MaterialTranslation;
import kg.miras.backend.catalog.dto.request.MaterialDraftRequest;
import org.mapstruct.AfterMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = MaterialTranslationMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MaterialMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdById", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "authors", ignore = true)
    Material toEntity(MaterialDraftRequest source);

    @AfterMapping
    default void attachTranslations(@MappingTarget Material material) {
        if (material.getTranslations() != null) {
            for (MaterialTranslation translation : material.getTranslations()) {
                translation.setMaterial(material);
            }
        }
    }
}
