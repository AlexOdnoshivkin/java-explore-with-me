package ru.practicum.ewmservice.services;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.exceptions.DataConflictException;
import ru.practicum.ewmservice.exceptions.EntityNotFoundException;
import ru.practicum.ewmservice.models.category.dto.CategoryDto;
import ru.practicum.ewmservice.models.category.dto.NewCategoryDto;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CategoryServiceImplTest {
    private final EntityManager em;

    private final CategoryService categoryService;

    @AfterEach
    void afterEach() {
        em.createNativeQuery("truncate table categories");
    }

    @Test
    void addNewCategoryWhenValidArguments() {
        // Проверяем корректный сценарий
        NewCategoryDto newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Test Category");

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(newCategoryDto.getName());

        CategoryDto savedCategory = categoryService.addNewCategory(newCategoryDto);
        categoryDto.setId(savedCategory.getId());

        assertEquals(categoryDto, savedCategory);
    }

    @Test
    void addNewCategoryWhenNameIsUsedThenThrowException() {
        // Проверяем случай, если подборка с таким именем уже существует
        NewCategoryDto newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Test Category");
        categoryService.addNewCategory(newCategoryDto);

        NewCategoryDto failedCategory = new NewCategoryDto();
        failedCategory.setName("Test Category");

        DataConflictException thrown = Assertions
                .assertThrows(DataConflictException.class, () ->
                        categoryService.addNewCategory(failedCategory));

        assertEquals("Категория с именем Test Category уже существует", thrown.getMessage());
    }

    @Test
    void patchCategoryWhenValidArguments() {
        // Проверяем корректный сценарий
        NewCategoryDto newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Test Category");
        CategoryDto savedCategory = categoryService.addNewCategory(newCategoryDto);

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Updated Name");
        categoryDto.setId(savedCategory.getId());

        CategoryDto patchedCategory = categoryService.patchCategory(categoryDto);

        assertEquals(categoryDto, patchedCategory);
    }

    @Test
    void patchCategoryWhenCategoryIsNotFoundThenThrowException() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Updated Name");
        categoryDto.setId(1L);

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        categoryService.patchCategory(categoryDto));

        assertEquals("Категория с id 1 не найдена в базе данных", thrown.getMessage());
    }

    @Test
    void patchCategoryWhenCategoryNameIsExistThenThroeException() {
        // Проверяем сценарий, когда категория с таким именем уже существует
        NewCategoryDto newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Test Category");
       CategoryDto savedCategory = categoryService.addNewCategory(newCategoryDto);

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Test Category");
        categoryDto.setId(savedCategory.getId());

        DataConflictException thrown = Assertions
                .assertThrows(DataConflictException.class, () ->
                        categoryService.patchCategory(categoryDto));

        assertEquals("Категория с именем Test Category уже существует", thrown.getMessage());
    }

    @Test
    void deleteCategoryWhenValidArguments() {
        // Проверяем корректный сценарий
        NewCategoryDto newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Test Category");
        CategoryDto savedCategory = categoryService.addNewCategory(newCategoryDto);

        categoryService.deleteCategory(savedCategory.getId());

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        categoryService.getCategory(1L));

        assertEquals("Категория с id 1 не найдена в базе данных", thrown.getMessage());
    }

    @Test
    void getCategories() {
        NewCategoryDto newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Test Category");
        CategoryDto savedCategory = categoryService.addNewCategory(newCategoryDto);

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Test Category");
        categoryDto.setId(savedCategory.getId());

        List<CategoryDto> categories = categoryService.getCategories(0, 10);

        assertEquals(List.of(categoryDto), categories);
    }

    @Test
    void getCategoryWhenValidArguments() {
        NewCategoryDto newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Test Category");
        CategoryDto savedCategory = categoryService.addNewCategory(newCategoryDto);

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Test Category");
        categoryDto.setId(savedCategory.getId());

        CategoryDto result = categoryService.getCategory(savedCategory.getId());

        assertEquals(categoryDto, result);
    }
}