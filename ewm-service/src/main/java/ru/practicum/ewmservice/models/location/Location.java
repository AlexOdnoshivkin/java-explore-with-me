package ru.practicum.ewmservice.models.location;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class Location {
    @NotNull
    private Double lat;
    @NotNull
    private Double lon;
}
