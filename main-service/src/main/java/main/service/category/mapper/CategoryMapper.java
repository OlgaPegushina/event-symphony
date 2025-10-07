package main.service.category.mapper;

import main.service.category.dto.CategoryDto;
import main.service.category.dto.NewCategoryDto;
import main.service.category.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toCategoryDto(Category category);

    @Mapping(target = "id", ignore = true)
    Category toCategory(NewCategoryDto newCategoryDto);

    @Mapping(target = "id", ignore = true)
    void updateCategoryFromDto(CategoryDto categoryDto, @MappingTarget Category category);

    default Category toEntity(Long id) {
        if (id == null) {
            return null;
        }
        Category c = new Category();
        c.setId(id);
        return c;
    }
}