package ru.practicum.ewmservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.FromSizeRequest;
import ru.practicum.ewmservice.exceptions.EntityNotFoundException;
import ru.practicum.ewmservice.models.location.Location;
import ru.practicum.ewmservice.models.location.dto.LocationDto;
import ru.practicum.ewmservice.models.location.dto.LocationMapper;
import ru.practicum.ewmservice.repositories.LocationRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public LocationDto addLocation(LocationDto locationDto) {
        Location location = LocationMapper.toLocationFromLocationDto(locationDto);
        LocationDto result = LocationMapper.toLocationDtoFromLocation(locationRepository.save(location));
        log.debug("Локация сохранена в базе данных: {}", location);
        return result;
    }

    @Override
    public List<LocationDto> getAllLocations(int from, int size) {
        Pageable pageable = FromSizeRequest.of(from, size);
        List<LocationDto> result = locationRepository.findLocationsWhereNameNotNull(pageable).stream()
                .map(LocationMapper::toLocationDtoFromLocation)
                .collect(Collectors.toList());
        log.debug("Получен список локаций из быза данных: {}", result);
        return result;
    }

    @Override
    @Transactional
    public void deleteLocationById(Long id) {
        checkLocation(id);
        locationRepository.deleteById(id);
        log.debug("Локация с id " + id + " удалена из базы данных");
    }

    public Optional<Location> getLocationByCoordinate(double lat, double lon) {
        return locationRepository
                .findLocationByCoordinate(lat, lon);
    }

    public void compareLocation(Location location) {
        Location savedLocation = checkLocation(location.getId());
        if (savedLocation.equals(location)) {
            throw new IllegalStateException("Невереные параметры локации");
        }
    }

    public Location checkLocation(Long id) {
        Optional<Location> locationOptional = locationRepository.findById(id);
        if (locationOptional.isEmpty()) {
            throw new EntityNotFoundException("Локация с id " + id + " не найдена в базе данных");
        }
        return locationOptional.get();
    }
}
