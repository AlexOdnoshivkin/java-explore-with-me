package ru.practicum.ewmservice.models.category.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CategoryDto {
    @NotNull
    private Long id;
    @NotBlank
    private String name;
}
