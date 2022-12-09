package ru.practicum.ewmservice.services;

import ru.practicum.ewmservice.models.location.dto.LocationDto;

import java.util.List;

public interface LocationService {
    LocationDto addLocation(LocationDto locationDto);

    List<LocationDto> getAllLocations(int from, int size);

    void deleteLocationById(Long id);
}
