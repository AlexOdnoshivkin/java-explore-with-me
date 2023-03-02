package ru.practicum.ewmservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.FromSizeRequest;
import ru.practicum.ewmservice.exceptions.EntityNotFoundException;
import ru.practicum.ewmservice.models.event.Event;
import ru.practicum.ewmservice.models.location.Location;
import ru.practicum.ewmservice.models.location.dto.LocationDto;
import ru.practicum.ewmservice.models.location.dto.LocationMapper;
import ru.practicum.ewmservice.repositories.EventRepository;
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

    private final EventRepository eventRepository;

    @Override
    @Transactional
    public LocationDto addLocation(LocationDto locationDto) {
        if (getLocationByCoordinate(locationDto.getLat(), locationDto.getLon()).isPresent()) {
            throw new IllegalStateException("По координатам " + locationDto.getLat() + ", " + locationDto.getLon() +
                    " в базе данных уже есть запись локации");
        }
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
        Location location = checkLocation(id);
        List<Event> events = eventRepository.getEventsByLocation(location.getLat(), location.getLon());
        log.debug("Найденные события в локации: {}", events);
        if (events.size() != 0) {
            throw new IllegalStateException("Локация не может быть удалена, пока к ней привязаны события");
        }
        locationRepository.deleteById(id);
        log.debug("Локация с id " + id + " удалена из базы данных");
    }

    public Optional<Location> getLocationByCoordinate(double lat, double lon) {
        return locationRepository
                .findLocationByCoordinate(lat, lon);
    }

    public void compareLocation(LocationDto locationDto) {
        Location savedLocation = checkLocation(locationDto.getId());
        if (savedLocation.equals(LocationMapper.toLocationFromLocationDto(locationDto))) {
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
