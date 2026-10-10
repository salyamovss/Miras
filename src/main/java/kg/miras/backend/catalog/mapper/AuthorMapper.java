package kg.miras.backend.catalog.mapper;

import java.util.List;
import kg.miras.backend.catalog.dal.entity.Author;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AuthorMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "displayName", source = ".")
    Author toEntity(String displayName);

    List<Author> toEntities(List<String> displayNames);
}
