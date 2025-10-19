package event.service.category.service;

import interaction.api.exception.ConflictException;
import interaction.api.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import interaction.api.dto.category.CategoryDto;
import interaction.api.dto.category.NewCategoryDto;
import event.service.category.mapper.CategoryMapper;
import event.service.category.model.Category;
import event.service.category.repository.CategoryRepository;
import event.service.events.model.EventModel;
import event.service.events.services.PublicService;
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

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }


    private void validateNameExist(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new ConflictException("Название категории уже существует");
        }
    }
}