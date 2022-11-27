package ru.practicum.ewmservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.FromSizeRequest;
import ru.practicum.ewmservice.exceptions.DataConflictException;
import ru.practicum.ewmservice.exceptions.EntityNotFoundException;
import ru.practicum.ewmservice.models.category.Category;
import ru.practicum.ewmservice.models.category.dto.CategoryDto;
import ru.practicum.ewmservice.models.category.dto.CategoryMapper;
import ru.practicum.ewmservice.models.category.dto.NewCategoryDto;
import ru.practicum.ewmservice.models.event.Event;
import ru.practicum.ewmservice.repositories.CategoryRepository;
import ru.practicum.ewmservice.repositories.EventRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto addNewCategory(NewCategoryDto newCategoryDto) {
        checkCategoryName(newCategoryDto.getName());
        Category newCategory = CategoryMapper.toCategoryFromNewCategoryDto(newCategoryDto);
        CategoryDto savedCategory = CategoryMapper.toCategoryDtoFromCategory(categoryRepository.save(newCategory));
        log.debug("Категория {} сохранена в базе данных", savedCategory);
        return savedCategory;
    }

    @Override
    @Transactional
    public CategoryDto patchCategory(CategoryDto categoryDto) {
        Category category = CategoryMapper.toCategoryFromCategoryDto(categoryDto);
        checkCategoryInDatabase(category.getId());
        checkCategoryName(categoryDto.getName());
        CategoryDto patchedCategory = CategoryMapper.toCategoryDtoFromCategory(categoryRepository.save(category));
        log.debug("Категория {} изменена в базе данных", patchedCategory);
        return patchedCategory;
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        checkCategoryInDatabase(categoryId);
        List<Event> eventsByCategory = eventRepository.getEventsByCategory(categoryId);
        if (!eventsByCategory.isEmpty()) {
            throw new IllegalStateException("Невозможно удалить категорию, к которой привязано событие");
        }
        categoryRepository.deleteCategoryById(categoryId);
        log.info("Категория удалениа из базы данных id: {}", categoryId);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = FromSizeRequest.of(from, size);
        List<CategoryDto> result = categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::toCategoryDtoFromCategory)
                .collect(Collectors.toList());
        log.info("Получены категории из базы данных: {}", result);
        return result;
    }

    @Override
    public CategoryDto getCategory(Long categoryId) {
        Category category = checkCategoryInDatabase(categoryId);
        CategoryDto result = CategoryMapper.toCategoryDtoFromCategory(category);
        log.debug("Категория получена из базы данных {}", result);
        return result;
    }

    private Category checkCategoryInDatabase(Long categoryId) {
        Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            throw new EntityNotFoundException("Категория с id {} не найдена в базе данных");
        }
        return categoryOptional.get();
    }

    private void checkCategoryName(String categoryName) {
        Optional<Category> categoryOptional = categoryRepository.getCategoryByName(categoryName);
        if (categoryOptional.isPresent()) {
            throw new DataConflictException("Категория с именем " + categoryName + " уже существует");
        }
    }
}
