package ru.practicum.ewmservice.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.models.event.dto.EventFullDto;
import ru.practicum.ewmservice.models.event.dto.EventShortDto;
import ru.practicum.ewmservice.models.event.dto.NewEventDto;
import ru.practicum.ewmservice.models.event.dto.UpdateEventRequest;
import ru.practicum.ewmservice.models.participation_request.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.services.EventService;
import ru.practicum.ewmservice.services.ParticipationRequestService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PrivateController {

    private final EventService eventService;

    private final ParticipationRequestService requestService;

    @PostMapping("/users/{userId}/events")
    public EventFullDto addNewEvent(@PathVariable(name = "userId") Long userId,
                                    @RequestBody @Validated NewEventDto newEventDto) {
        log.info("Получен запрос на добавление события {} пользователем с id {}", newEventDto, userId);
        return eventService.addNewEvent(newEventDto, userId);
    }

    @PostMapping("/users/{userId}/requests")
    public ParticipationRequestDto addNewParticipationRequest(@PathVariable(name = "userId") Long userId,
                                                              @RequestParam(name = "eventId") Long eventId) {
        log.info("Получен запрос на добавление заявки на участие от пользователя с id {} на событие с id {}",
                userId, eventId);
        return requestService.addNewParticipationRequest(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events")
    public EventFullDto patchEvent(@PathVariable(name = "userId") Long userId,
                                   @RequestBody @Validated UpdateEventRequest updateEventRequest) {
        log.info("Получен запрос на изменение события {} пользователем с id {}", updateEventRequest, userId);
        return eventService.patchEvent(updateEventRequest, userId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto cancelEventFromUser(@PathVariable(name = "userId") Long userId,
                                            @PathVariable(name = "eventId") Long eventId) {
        log.info("Получен запрос на отмену события с id {} пользователем с id {}", eventId, userId);
        return eventService.cancelEventFromUser(userId, eventId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelParticipationRequestByUser(@PathVariable(name = "userId") Long userId,
                                                                    @PathVariable(name = "requestId") Long requestId) {
        log.info("Получен запрос на отмену запроса на участие с id {} пользователем с id {}", requestId, userId);
        return requestService.cancelParticipationRequestByUser(userId, requestId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests/{reqId}/confirm")
    public ParticipationRequestDto confirmParticipationRequest(@PathVariable(name = "userId") Long userId,
                                                               @PathVariable(name = "eventId") Long eventId,
                                                               @PathVariable(name = "reqId") Long requestId) {
        log.info("Получен запрос на подтверждение заявки с id {} пользователем с id {} к событию с id {}",
                requestId, userId, eventId);
        return requestService.confirmParticipationRequest(userId, eventId, requestId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests/{reqId}/reject")
    public ParticipationRequestDto rejectParticipationRequest(@PathVariable(name = "userId") Long userId,
                                                              @PathVariable(name = "eventId") Long eventId,
                                                              @PathVariable(name = "reqId") Long requestId) {
        log.info("Получен запрос на отклонение заявки с id {} пользователем с id {} к событию с id {}",
                requestId, userId, eventId);
        return requestService.rejectParticipationRequest(userId, eventId, requestId);
    }

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getUserEvents(@PositiveOrZero
                                             @RequestParam(name = "from", defaultValue = "0") Integer from,
                                             @Positive
                                             @RequestParam(name = "size", defaultValue = "10")
                                             Integer size,
                                             @PathVariable(name = "userId") Long userId) {
        log.info("Получен запрос на получение списка событий текущего пользователя с id {}", userId);
        return eventService.getUserEvents(from, size, userId);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getUserEvent(@PathVariable(name = "userId") Long userId,
                                     @PathVariable(name = "eventId") Long eventId) {
        log.info("Получен запрос на получение подробной информации о событии с id {} пользователем {}",
                userId, eventId);
        return eventService.getUserEventFullInfo(userId, eventId);
    }

    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> getParticipationRequestsByUser(@PathVariable(name = "userId") Long userId) {
        log.info("Получен запрос на вывод запросов на участия в событиях пользователя с id {}", userId);
        return requestService.getUserParticipationRequests(userId);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByUserEvent(@PathVariable(name = "userId") Long userId,
                                                                @PathVariable(name = "eventId") Long eventId) {
        log.info("Получен запрос на получение заявок на участие в событии с id: {} пользовател с id: {}",
                eventId, userId);
        return requestService.getUserEventParticipationRequests(userId, eventId);
    }
}
