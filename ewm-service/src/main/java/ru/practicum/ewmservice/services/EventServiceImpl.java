package ru.practicum.ewmservice.services;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.FromSizeRequest;
import ru.practicum.ewmservice.exceptions.EntityNotFoundException;
import ru.practicum.ewmservice.httpClient.HttpClient;
import ru.practicum.ewmservice.models.category.Category;
import ru.practicum.ewmservice.models.event.Event;
import ru.practicum.ewmservice.models.event.QEvent;
import ru.practicum.ewmservice.models.event.State;
import ru.practicum.ewmservice.models.event.dto.*;
import ru.practicum.ewmservice.models.httpClientRequestEntity.ViewStats;
import ru.practicum.ewmservice.models.user.User;
import ru.practicum.ewmservice.repositories.CategoryRepository;
import ru.practicum.ewmservice.repositories.EventRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
        log.debug("?????????????? {} ?????????????????? ?? ???????? ????????????", savedEvent);
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
            throw new IllegalStateException("?????????????? ???? ???????????????? PUBLISHED ???? ?????????? ???????? ????????????????");
        }

        mapper.updateEvent(updateEventRequest, savedEvent);
        savedEvent.setState(State.PENDING);
        EventFullDto updatedEvent = mapper.toEventFullDtoFromEvent(eventRepository.save(savedEvent));
        log.debug("?????????????????? ?????????????? {} ?? ???????? ????????????", updatedEvent);
        return updatedEvent;
    }

    @Override
    public List<EventShortDto> getUserEvents(int from, int size, Long userId) {
        userService.checkUserInDatabase(userId);
        Pageable pageable = FromSizeRequest.of(from, size);

        List<EventShortDto> result = eventRepository.getUserEvents(userId, pageable).stream()
                .map(mapper::toEventShortDtoFromEvent)
                .collect(Collectors.toList());

        log.debug("?????????????? ???????????? ?????????????? ???????????????????????? ?? id {} : {}", userId, result);
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
        log.debug("?????????????? ???????????????? ?????????????????????????? ?? id: {} : {}", userId, result);
        return result;
    }

    @Override
    @Transactional
    public EventFullDto publishEvent(Long eventId) {
        Event event = checkEventInDatabase(eventId);
        event.setState(State.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        EventFullDto result = mapper.toEventFullDtoFromEvent(eventRepository.save(event));
        log.debug("???????????????????????? ?????????????? {}", result);
        return result;
    }

    @Override
    @Transactional
    public EventFullDto rejectEvent(Long eventId) {
        Event event = checkEventInDatabase(eventId);
        event.setState(State.CANCELED);
        EventFullDto result = mapper.toEventFullDtoFromEvent(eventRepository.save(event));
        log.debug("?????????????????? ?????????????? {}", result);
        return result;
    }

    @Override
    public List<EventShortDto> searchEvents(String text, Long[] categories, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, boolean isAvailable, String sort, int from,
                                            int size, String ip, String uri) {
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeStart = LocalDateTime.now().plusYears(100);
        }
        Pageable pageable = setPageable(from, size, sort);

        BooleanExpression byAnnotation;
        BooleanExpression byDescription;
        BooleanExpression byPaid;
        BooleanExpression byAvailable;
        BooleanExpression byCategories;
        BooleanExpression byEventDate = QEvent.event.eventDate.between(rangeStart, rangeEnd);

        if (text == null) {
            byAnnotation = QEvent.event.annotation.ne("");
            byDescription = QEvent.event.description.ne("");
        } else {
            byAnnotation = QEvent.event.annotation.containsIgnoreCase(text);
            byDescription = QEvent.event.description.containsIgnoreCase(text);
        }
        if (categories == null) {
            byCategories = QEvent.event.category.id.ne(0L);
        } else {
            byCategories = QEvent.event.category.id.in(categories);
        }
        if (paid == null) {
            byPaid = QEvent.event.paid.isTrue().or(QEvent.event.paid.isFalse());
        } else {
            byPaid = QEvent.event.paid.eq(paid);
        }
        if (isAvailable) {
            byAvailable = QEvent.event.confirmedRequests.lt(QEvent.event.participantLimit);
        } else {
            byAvailable = QEvent.event.id.ne(0L);
        }


        Iterable<Event> foundEvents = eventRepository.findAll(byAnnotation.or(byDescription).and(byCategories)
                .and(byPaid).and(byAvailable).and(byEventDate), pageable);
        List<EventShortDto> result = StreamSupport.stream(foundEvents.spliterator(), false)
                .map(mapper::toEventShortDtoFromEvent)
                .collect(Collectors.toList());

        String[] uris = result.stream().map((EventShortDto e) -> ("/event/" + e.getId())).toArray(String[]::new);

        Map<String, ViewStats> viewStatsMap = httpClient.getStat(LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100), uris, true);
        if (viewStatsMap != null) {
            result = result.stream()
                    .peek((e -> {
                        if (viewStatsMap.get("/event/" + e.getId()) != null) {
                            e.setViews(Math.toIntExact(viewStatsMap.get("/event/" + e.getId()).getHits()));
                        }
                    })).collect(Collectors.toList());
        }
        log.info("???????????? ???????????? ?????????????? ?? ???????? ????????????: {}", result);
        httpClient.postStat(null, uri, ip);
        return result;
    }

    @Override
    public EventFullDto getEventFullInfoById(Long eventId, String ip, String uri) {
        Event event = checkEventInDatabase(eventId);
        httpClient.postStat(event.getId(), uri, ip);
        eventRepository.save(event);
        EventFullDto result = mapper.toEventFullDtoFromEvent(event);
        Map<String, ViewStats> viewStatsMap = httpClient.getStat(LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100), new String[]{uri}, true);
        if (viewStatsMap != null) {
            Long views = viewStatsMap.get(uri).getHits();
            result.setViews(Math.toIntExact(views));
        }
        log.debug("???????????????? ?????????????? ???? ???????? ????????????: {}", result);
        return result;
    }

    @Override
    @Transactional
    public EventFullDto putEventByAdmin(Long eventId, AdminUpdateEventRequest adminUpdateEventRequest) {
        Event savedEvent = checkEventInDatabase(eventId);
        mapper.updateEventFromAdmin(adminUpdateEventRequest, savedEvent);
        EventFullDto result = mapper.toEventFullDtoFromEvent(eventRepository.save(savedEvent));
        log.debug("?????????????? ?????????????????? ??????????????????????????????: {}", result);
        return result;
    }

    @Override
    public List<EventFullDto> searchEventByAdmin(Long[] users, State[] states, Long[] categories,
                                                 LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        Pageable pageable = setPageable(from, size, null);

        BooleanExpression byUserId;
        BooleanExpression byStates;
        BooleanExpression byCategories;
        BooleanExpression byEventDate;

        if (users != null) {
            byUserId = QEvent.event.initiator.id.in(users);
        } else {
            byUserId = QEvent.event.initiator.id.ne(0L);
        }
        if (states != null) {
            byStates = QEvent.event.state.in(states);
        } else {
            byStates = QEvent.event.state.isNotNull();
        }
        if (categories != null) {
            byCategories = QEvent.event.category.id.in(categories);
        } else {
            byCategories = QEvent.event.category.id.ne(0L);
        }
        if (rangeEnd == null && rangeStart == null) {
            byEventDate = QEvent.event.eventDate.after(LocalDateTime.now().minusYears(100));
        } else {
            byEventDate = QEvent.event.eventDate.between(rangeStart, rangeEnd);
        }

        Iterable<Event> foundEvents = eventRepository.findAll(byCategories.and(byUserId).and(byStates)
                .and(byEventDate), pageable);

        List<EventFullDto> result = StreamSupport.stream(foundEvents.spliterator(), false)
                .map(mapper::toEventFullDtoFromEvent)
                .collect(Collectors.toList());
        String[] uris = result.stream().map((EventFullDto e) -> ("/event/" + e.getId())).toArray(String[]::new);

        Map<String, ViewStats> viewStatsMap = httpClient.getStat(LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100), uris, true);
        if (viewStatsMap != null) {
            result = result.stream()
                    .peek((e -> {
                        if (viewStatsMap.get("/event/" + e.getId()) != null) {
                            e.setViews(Math.toIntExact(viewStatsMap.get("/event/" + e.getId()).getHits()));
                        }
                    })).collect(Collectors.toList());
        }

        log.debug("???????????? ???????????? ?????????????? ?? ???????? ????????????: {}", result);
        return result;
    }

    private Category checkCategoryInDatabase(Long categoryId) {
        Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            throw new
                    EntityNotFoundException("?????????????????? ?? id " + categoryId + " ???? ???????????? ?? ???????? ????????????");
        }
        return categoryOptional.get();
    }

    public Event checkEventInDatabase(Long eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new
                    EntityNotFoundException("?????????????????? ?? id " + eventId + " ???? ???????????? ?? ???????? ????????????");
        }
        return eventOptional.get();
    }

    private void checkIsEventInitiator(Long userId, Event event) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new IllegalStateException("???????????????????????? ?? id " + userId + " ???? ???????????????? ?????????????????????? ??????????????");
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
                throw new IllegalStateException("???????????????????? ???????????? ???????????????????? ???? ????????????????????");
        }
    }
}
