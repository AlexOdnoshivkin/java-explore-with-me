package ru.practicum.ewmservice.models.location.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LocationDto {
    private Long id;
    private String name;
    private Double radius = 0.0;
    @NotNull
    private Double lat;
    @NotNull
    private Double lon;
}
