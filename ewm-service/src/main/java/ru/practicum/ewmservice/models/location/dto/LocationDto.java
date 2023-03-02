package ru.practicum.ewmservice.models.location.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
public class LocationDto {
    private Long id;
    private String name;
    @PositiveOrZero
    private Double radius = 0.0;
    @NotNull
    @PositiveOrZero
    private Double lat;
    @NotNull
    @PositiveOrZero
    private Double lon;
}
