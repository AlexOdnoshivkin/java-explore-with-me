package ru.practicum.ewmservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.FromSizeRequest;
import ru.practicum.ewmservice.exceptions.EntityNotFoundException;
import ru.practicum.ewmservice.httpClient.HttpClient;
import ru.practicum.ewmservice.models.httpClientRequestEntity.ViewStats;
import ru.practicum.ewmservice.models.category.Category;
import ru.practicum.ewmservice.models.event.Event;
import ru.practicum.ewmservice.models.event.State;
import ru.practicum.ewmservice.models.event.dto.*;
import ru.practicum.ewmservice.models.user.User;
import ru.practicum.ewmservice.repositories.CategoryRepository;
import ru.practicum.ewmservice.repositories.EventRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final CategoryRepository categoryRepository;

    private final UserServiceImpl userService;

    private final EventMapper mapper;

    private final HttpClient httpClient;

    @Override
    @Transactional
    public EventFullDto addNewEvent(NewEventDto newEventDto, Long userId) {
        User user = userService.checkUserInDatabase(userId);
        Category category = checkCategoryInDatabase(newEventDto.getCategory());

        Event event = mapper.toEventFromNewEventDto(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(State.PENDING);

        EventFullDto savedEvent = mapper.toEventFullDtoFromEvent(eventRepository.save(event));
        log.debug("Событие {} сохранено в базе данных", savedEvent);
        log.debug("{}", eventRepository.findById(savedEvent.getId()));
        return savedEvent;
    }

    @Override
    @Transactional
    public EventFullDto patchEvent(UpdateEventRequest updateEventRequest, Long userId) {
        Event savedEvent = checkEventInDatabase(updateEventRequest.getEventId());
        userService.checkUserInDatabase(userId);
        checkIsEventInitiator(userId, savedEvent);

        if (savedEvent.getState().equals(State.PUBLISHED)) {
            throw new IllegalStateException("События со статусом PUBLISHED не могут быть изменены");
        }

        mapper.updateEvent(updateEventRequest, savedEvent);
        savedEvent.setState(State.PENDING);
        EventFullDto updatedEvent = mapper.toEventFullDtoFromEvent(eventRepository.save(savedEvent));
        log.debug("Обновлено событие {} в базе данных", updatedEvent);
        return updatedEvent;
    }

    @Override
    public List<EventShortDto> getUserEvents(int from, int size, Long userId) {
        userService.checkUserInDatabase(userId);
        Pageable pageable = FromSizeRequest.of(from, size);

        List<EventShortDto> result = eventRepository.getUserEvents(userId, pageable).stream()
                .map(mapper::toEventShortDtoFromEvent)
                .collect(Collectors.toList());

        log.debug("Получен список событий пользователя с id {} : {}", userId, result);
        return result;
    }

    @Override
    public EventFullDto getUserEventFullInfo(Long userId, Long eventId) {
        userService.checkUserInDatabase(userId);
        Event event = checkEventInDatabase(eventId);
        checkIsEventInitiator(userId, event);

        return mapper.toEventFullDtoFromEvent(event);
    }

    @Override
    @Transactional
    public EventFullDto cancelEventFromUser(Long userId, Long eventId) {
        userService.checkUserInDatabase(userId);
        Event event = checkEventInDatabase(eventId);
        checkIsEventInitiator(userId, event);
        event.setState(State.CANCELED);
        EventFullDto result = mapper.toEventFullDtoFromEvent(eventRepository.save(event));
        log.debug("Событие отменено пользователем с id: {} : {}", userId, result);
        return result;
    }

    @Override
    @Transactional
    public EventFullDto publishEvent(Long eventId) {
        Event event = checkEventInDatabase(eventId);
        event.setState(State.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        EventFullDto result = mapper.toEventFullDtoFromEvent(eventRepository.save(event));
        log.debug("Опубликовано событие {}", result);
        return result;
    }

    @Override
    @Transactional
    public EventFullDto rejectEvent(Long eventId) {
        Event event = checkEventInDatabase(eventId);
        event.setState(State.CANCELED);
        EventFullDto result = mapper.toEventFullDtoFromEvent(eventRepository.save(event));
        log.debug("Отклонено событие {}", result);
        return result;
    }

    @Override
    public List<EventShortDto> searchEvents(String text, Long[] categories, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, boolean isAvailable, String sort, int from,
                                            int size, String ip, String uri) {
        List<EventShortDto> result;
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeStart = LocalDateTime.now().plusYears(100);
        }
        Pageable pageable = setPageable(from, size, sort);

        if (paid == null) {
            result = eventRepository.searchEventsWithoutPaid(text, categories, rangeStart, rangeEnd,
                            isAvailable, pageable).stream()
                    .map(mapper::toEventShortDtoFromEvent)
                    .collect(Collectors.toList());
        } else {
            result = eventRepository.searchEventsWithPaid(text, categories, paid, rangeStart, rangeEnd,
                            isAvailable, pageable).stream()
                    .map(mapper::toEventShortDtoFromEvent)
                    .collect(Collectors.toList());
        }
        log.info("Найден список событий в базе данных: {}", result);
        httpClient.postStat(null, uri, ip);
        return result;
    }

    @Override
    public EventFullDto getEventFullInfoById(Long eventId, String ip, String uri) {
        Event event = checkEventInDatabase(eventId);
        httpClient.postStat(event.getId(), uri, ip);
        Map<String, ViewStats> viewStatsMap = httpClient.getStat(LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100), new String[]{uri, uri}, true);
        Long views = viewStatsMap.get(uri).getHits();
        event.setViews(Math.toIntExact(views));
        eventRepository.save(event);
        EventFullDto result = mapper.toEventFullDtoFromEvent(event);
        log.debug("Получено событие из базы данных: {}", result);
        return result;
    }

    @Override
    @Transactional
    public EventFullDto putEventByAdmin(Long eventId, AdminUpdateEventRequest adminUpdateEventRequest) {
        Event savedEvent = checkEventInDatabase(eventId);
        mapper.updateEventFromAdmin(adminUpdateEventRequest, savedEvent);
        EventFullDto result = mapper.toEventFullDtoFromEvent(eventRepository.save(savedEvent));
        log.debug("Событие обновлено администратором: {}", result);
        return result;
    }

    @Override
    public List<EventFullDto> searchEventByAdmin(Long[] users, State[] states, Long[] categories,
                                                 LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        Pageable pageable = setPageable(from, size, null);

        List<EventFullDto> result = eventRepository
                .searchEventsWithUsersAndCategoryAndStates(users, states, categories, rangeStart, rangeEnd, pageable)
                .stream()
                .map(mapper::toEventFullDtoFromEvent)
                .collect(Collectors.toList());
        log.debug("Найден список событий в базе данных: {}", result);
        return result;
    }

    private Category checkCategoryInDatabase(Long categoryId) {
        Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            throw new
                    EntityNotFoundException("Категория с id " + categoryId + " не нйдена в базе данных");
        }
        return categoryOptional.get();
    }

    public Event checkEventInDatabase(Long eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new
                    EntityNotFoundException("Категория с id " + eventId + " не нйдена в базе данных");
        }
        return eventOptional.get();
    }

    private void checkIsEventInitiator(Long userId, Event event) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new IllegalStateException("Пользователь с id " + userId + " не является инициатором события");
        }
    }

    private Pageable setPageable(int from, int size, String sort) {
        switch (String.valueOf(sort)) {
            case "EVENT_DATE":
                sort = "eventDate";
                return FromSizeRequest.of(from, size, Sort.by(sort).ascending());
            case "VIEWS":
                return FromSizeRequest.of(from, size, Sort.by(sort.toLowerCase()).ascending());
            case "null":
                return FromSizeRequest.of(from, size, null);
            default:
                throw new IllegalStateException("Указанного метода сортировки не существует");
        }
    }
}
