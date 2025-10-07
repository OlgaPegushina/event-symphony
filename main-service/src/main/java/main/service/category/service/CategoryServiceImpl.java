package main.service.category.service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.service.category.dto.CategoryDto;
import main.service.category.dto.NewCategoryDto;
import main.service.category.mapper.CategoryMapper;
import main.service.category.model.Category;
import main.service.category.repository.CategoryRepository;
import main.service.events.model.EventModel;
import main.service.events.services.PublicService;
import main.service.exception.ConflictException;
import main.service.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
    PublicService eventService;
    CategoryMapper mapper;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        validateNameExist(newCategoryDto.getName());
        return mapper.toCategoryDto(categoryRepository.save(mapper.toCategory(newCategoryDto)));
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        List<EventModel> events = eventService.findAllByCategoryId(catId);
        if (events.isEmpty()) {
            categoryRepository.deleteById(catId);
        } else {
            throw new ConflictException("Категория не может быть удалена пока содержит события");
        }
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + catId + " не найдена"));
        if (!category.getName().equals(categoryDto.getName())) {
            validateNameExist(categoryDto.getName());
        }
        mapper.updateCategoryFromDto(categoryDto, category);
        return mapper.toCategoryDto(category);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return categoryRepository.findAll().stream()
                .map(mapper::toCategoryDto)
                .skip(from)
                .limit(size)
                .toList();
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        return mapper.toCategoryDto(
                categoryRepository.findById(catId)
                        .orElseThrow(() -> new NotFoundException("Категория с id " + catId + " не найдена")));
    }

    private void validateNameExist(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new ConflictException("Название категории уже существует");
        }
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }
}