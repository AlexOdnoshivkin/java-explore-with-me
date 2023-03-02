package ru.practicum.ewmservice.services;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.exceptions.EntityNotFoundException;
import ru.practicum.ewmservice.models.location.dto.LocationDto;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class LocationServiceImplTest {
    private final EntityManager em;

    private final LocationService locationService;

    @AfterEach
    void afterEach() {
        em.createNativeQuery("truncate table locations");
    }

    @Test
    void addLocationWhenValidArguments() {
        // Проверка корректного сценария
        LocationDto locationDto = new LocationDto();
        locationDto.setName("Test Location");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);

        LocationDto result = locationService.addLocation(locationDto);
        locationDto.setId(result.getId());

        assertEquals(locationDto, result);
    }

    @Test
    void addLocationWhenCoordinateIsAvailableInDatabaseThenThrowException() {
        // Проверка случая, когда по указанным координатам уже имеется запись локации в базе данных с учётом радиуса
        LocationDto locationDto = new LocationDto();
        locationDto.setName("First Location");
        locationDto.setLat(20.0);
        locationDto.setLon(20.0);
        locationDto.setRadius(100.0);

        locationService.addLocation(locationDto);

        locationDto.setName("Second Location");
        locationDto.setLat(20.1);
        locationDto.setLon(20.1);
        locationDto.setRadius(1.0);

        IllegalStateException thrown = Assertions
                .assertThrows(IllegalStateException.class, () ->
                        locationService.addLocation(locationDto));

        assertEquals("По координатам 20.1, 20.1 в базе данных уже есть запись локации", thrown.getMessage());
    }

    @Test
    void getAllLocations() {
        LocationDto locationDto = new LocationDto();
        locationDto.setName("First Location");
        locationDto.setLat(20.0);
        locationDto.setLon(20.0);
        locationDto.setRadius(10.0);

        LocationDto firstLocation = locationService.addLocation(locationDto);

        locationDto.setName("Second Location");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);

        LocationDto secondLocation = locationService.addLocation(locationDto);

        List<LocationDto> result = locationService.getAllLocations(0, 10);

        assertEquals(2, result.size());
        assertEquals(firstLocation.getName(), result.get(0).getName());
        assertEquals(secondLocation.getName(), result.get(1).getName());
    }

    @Test
    void deleteLocationByIdWhenValidArguments() {
        LocationDto locationDto = new LocationDto();
        locationDto.setName("Test Location");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);

        LocationDto location = locationService.addLocation(locationDto);

        locationService.deleteLocationById(location.getId());

        List<LocationDto> result = locationService.getAllLocations(0, 10);

        assertEquals(0, result.size());
    }

    @Test
    void deleteLocationByIdWhenLocationNotFoundThenThrowException() {
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        locationService.deleteLocationById(0L));

        assertEquals("Локация с id 0 не найдена в базе данных", thrown.getMessage());
    }
}