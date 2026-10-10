package kg.miras.backend.catalog.mapper;

import kg.miras.backend.catalog.dal.entity.MaterialTranslation;
import kg.miras.backend.catalog.dal.entity.MaterialTranslationId;
import kg.miras.backend.catalog.dto.request.MaterialTranslationDraftDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MaterialTranslationMapper {
    @Mapping(target = "id", expression = "java(createId(source.languageCode()))")
    @Mapping(target = "material", ignore = true)
    MaterialTranslation toEntity(MaterialTranslationDraftDto source);

    default MaterialTranslationId createId(String languageCode) {
        MaterialTranslationId id = new MaterialTranslationId();
        id.setLanguageCode(languageCode);
        return id;
    }
}
