package event.service.category.service;

import interaction.api.dto.category.CategoryDto;
import interaction.api.dto.category.NewCategoryDto;
import event.service.category.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long catId);

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto getCategory(Long catId);

    Optional<Category> findById(Long id);
}