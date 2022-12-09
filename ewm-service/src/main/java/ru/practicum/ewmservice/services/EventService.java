package ru.practicum.ewmservice.services;

import ru.practicum.ewmservice.models.event.State;
import ru.practicum.ewmservice.models.event.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto addNewEvent(NewEventDto newEventDto, Long userId);

    EventFullDto patchEvent(UpdateEventRequest updateEventRequest, Long userId);

    List<EventShortDto> getUserEvents(int from, int size, Long userId);

    EventFullDto getUserEventFullInfo(Long userId, Long eventId);

    EventFullDto cancelEventFromUser(Long userId, Long eventId);

    EventFullDto publishEvent(Long eventId);

    EventFullDto rejectEvent(Long eventId);

    List<EventShortDto> searchEvents(String text, Long[] categories, Boolean paid, LocalDateTime rangeStart,
                                     LocalDateTime rangeEnd, boolean isAvailable, String sort, int from,
                                     int size, String ip, String uri, Double lat, Double lon);

    EventFullDto getEventFullInfoById(Long eventId, String ip, String uri);

    EventFullDto putEventByAdmin(Long eventId, AdminUpdateEventRequest adminUpdateEventRequest);

    List<EventFullDto> searchEventByAdmin(Long[] users, State[] states, Long[] categories, LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd, int from, int size, Double lat, Double lon);

    List<EventShortDto> searchEventInLocation(double lat, double lon);

}
