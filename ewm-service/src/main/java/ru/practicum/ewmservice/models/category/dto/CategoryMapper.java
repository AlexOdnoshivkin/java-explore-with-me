package ru.practicum.ewmservice.models.category.dto;

import ru.practicum.ewmservice.models.category.Category;

public class CategoryMapper {
    //Подавление конструктора по умолчанию для достижения неинстанцируемости
    private CategoryMapper() {
        throw new AssertionError();
    }

    public static Category toCategoryFromNewCategoryDto(NewCategoryDto newCategoryDto) {
        Category category = new Category();
        category.setName(newCategoryDto.getName());
        return category;
    }

    public static CategoryDto toCategoryDtoFromCategory(Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        return categoryDto;
    }

    public static Category toCategoryFromCategoryDto(CategoryDto categoryDto) {
        Category category = new Category();
        category.setId(categoryDto.getId());
        category.setName(categoryDto.getName());
        return category;
    }
}
