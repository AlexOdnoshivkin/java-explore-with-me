package ru.practicum.ewmservice.models.location.dto;

import ru.practicum.ewmservice.models.location.Location;

public class LocationMapper {
    private LocationMapper() {
    }



    public static Location toLocationFromLocationDto(LocationDto locationDto) {
        Location location = new Location();
        location.setId(locationDto.getId());
        location.setName(locationDto.getName());
        location.setLat(locationDto.getLat());
        location.setLon(locationDto.getLon());
        location.setRadius(locationDto.getRadius());
        return location;
    }

    public static LocationDto toLocationDtoFromLocation(Location location) {
        LocationDto locationDto = new LocationDto();
        locationDto.setId(location.getId());
        locationDto.setName(location.getName());
        locationDto.setLat(location.getLat());
        locationDto.setLon(location.getLon());
        locationDto.setRadius(location.getRadius());
        return locationDto;
    }
}

