package ru.practicum.ewmservice.models.user.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserDto {
    private Long id;
    @NotNull
    private String email;
    @NotNull
    private String name;
}
