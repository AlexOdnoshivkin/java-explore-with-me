package ru.practicum.ewmservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmservice.models.category.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    void deleteCategoryById(Long categoryId);

    Optional<Category> getCategoryByName(String name);
}
