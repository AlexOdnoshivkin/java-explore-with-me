package ru.practicum.ewmservice.models.location.dto;

import lombok.Data;

@Data
public class LocationDto {
    private Long id;
    private String name;
    private Double radius = 0.0;
    private Double lat;
    private Double lon;
}
