package ru.practicum.ewmservice.services;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.exceptions.EntityNotFoundException;
import ru.practicum.ewmservice.models.category.dto.CategoryDto;
import ru.practicum.ewmservice.models.category.dto.NewCategoryDto;
import ru.practicum.ewmservice.models.event.State;
import ru.practicum.ewmservice.models.event.dto.*;
import ru.practicum.ewmservice.models.location.dto.LocationDto;
import ru.practicum.ewmservice.models.location.dto.LocationMapper;
import ru.practicum.ewmservice.models.user.dto.NewUserRequest;
import ru.practicum.ewmservice.models.user.dto.UserDto;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class EventServiceImplTest {
    private final EntityManager em;

    private final EventService eventService;

    private final UserService userService;

    private final CategoryService categoryService;

    private final LocationService locationService;

    private final EventMapper eventMapper;

    private UserDto user;

    private CategoryDto categoryDto;

    private LocationDto locationDto;

    @BeforeEach
    void setUp() {
        NewUserRequest newUser = new NewUserRequest();
        newUser.setName("Boris");
        newUser.setEmail("BorisBritva@gmail.com");
        user = userService.addNewUser(newUser);

        NewCategoryDto newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Test Category");
        categoryDto = categoryService.addNewCategory(newCategoryDto);

        locationDto = new LocationDto();
        locationDto.setName("Test Location");
        locationDto.setLat(100.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);
        locationService.addLocation(locationDto);

    }

    @AfterEach
    void afterEach() {
        em.createNativeQuery("truncate table events");
    }

    @Test
    void addNewEventWhenLocationIsNew() {
        // Проверка случая, если в событии указана локация, которой нет в базе данных
        NewEventDto newEventDto = setUpAddNewEvent();

        EventFullDto result = eventService.addNewEvent(newEventDto, user.getId());

        assertEquals(newEventDto.getTitle(), result.getTitle());
        assertEquals(State.PENDING, result.getState());
    }

    @Test
    void addNewEventWhenLocationCoordinateIsAlreadyAdded() {
        // Проверка случая, если в событии указана локация c координатами, которые соответствуют локации, уже имеющейся
        // в базе данных
        NewEventDto newEventDto = setUpAddNewEvent();
        LocationDto newLocation = new LocationDto();
        newLocation.setLat(100.0);
        newLocation.setLon(10.0);
        newEventDto.setLocation(newLocation);

        EventFullDto result = eventService.addNewEvent(newEventDto, user.getId());

        assertEquals(newEventDto.getTitle(), result.getTitle());
        assertEquals(State.PENDING, result.getState());
    }

    @Test
    void addNewEventWhenLocationIsAlreadyAdded() {
        // Проверка случая, если в событии указана локация, которая уже есть базе данных
        NewEventDto newEventDto = setUpAddNewEvent();
        newEventDto.setLocation(locationDto);

        EventFullDto result = eventService.addNewEvent(newEventDto, user.getId());

        assertEquals(newEventDto.getTitle(), result.getTitle());
        assertEquals(State.PENDING, result.getState());
    }

    @Test
    void addNewEventWhenUserIsNotFoundThenThrowException() {
        // Проверка случая, когда пользователь не найден в базе данных
        NewEventDto newEventDto = setUpAddNewEvent();

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        eventService.addNewEvent(newEventDto, 0L));

        assertEquals("Пользователь с id 0 не найден в базе данных", thrown.getMessage());
    }

    @Test
    void patchEventWhenValidData() {
        // Проверка корректного сценария
        UpdateEventRequest updateEventRequest = setUpPatchEvent();

        EventFullDto patchedEvent = eventService.patchEvent(updateEventRequest, user.getId());

        assertEquals(updateEventRequest.getAnnotation(), patchedEvent.getAnnotation());
        assertEquals(updateEventRequest.getCategory(), patchedEvent.getCategory().getId());
        assertEquals(updateEventRequest.getDescription(), patchedEvent.getDescription());
        assertEquals(updateEventRequest.getEventDate(), patchedEvent.getEventDate());
        assertEquals(updateEventRequest.isPaid(), patchedEvent.isPaid());
        assertEquals(updateEventRequest.getParticipantLimit(), patchedEvent.getParticipantLimit());
        assertEquals(updateEventRequest.getTitle(), patchedEvent.getTitle());
        assertEquals(State.PENDING, patchedEvent.getState());
    }

    @Test
    void patchEventWhenUserIsNotFoundThenThrowException() {
        // Проверка случая, когда пользователь не найден в базе данных
        UpdateEventRequest updateEventRequest = setUpPatchEvent();

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        eventService.patchEvent(updateEventRequest, 0L));

        assertEquals("Пользователь с id 0 не найден в базе данных", thrown.getMessage());
    }

    @Test
    void patchEventWhenEventNotFoundThenThrowException() {
        // Проверка случая, когда событие не найдено в базе данных
        UpdateEventRequest updateEventRequest = setUpPatchEvent();
        updateEventRequest.setEventId(0L);

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        eventService.patchEvent(updateEventRequest, user.getId()));

        assertEquals("Событие с id 0 не найдено в базе данных", thrown.getMessage());
    }

    @Test
    void patchEventWhenEventStateIsPublishedThenThrowException() {
        // Проверка случая, когда событие имеет статус "Опубликовано"
        UpdateEventRequest updateEventRequest = setUpPatchEvent();

        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(categoryDto.getId());
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        newEventDto.setTitle("Test Event");
        newEventDto.setLocation(locationDto);
        newEventDto.setTitle("012");
        newEventDto.setParticipantLimit(1);

        EventFullDto savedEvent = eventService.addNewEvent(newEventDto, user.getId());
        savedEvent = eventService.publishEvent(savedEvent.getId());

        updateEventRequest.setEventId(savedEvent.getId());

        IllegalStateException thrown = Assertions
                .assertThrows(IllegalStateException.class, () ->
                        eventService.patchEvent(updateEventRequest, user.getId()));

        assertEquals("События со статусом PUBLISHED не могут быть изменены", thrown.getMessage());
    }

    @Test
    void getUserEventsWhenValidData() {
        //Проверка корректного сценария

        NewUserRequest newUserRequest = new NewUserRequest();
        newUserRequest.setEmail("Another@gmail.com");
        newUserRequest.setName("AnotherName");

        UserDto anotherUser = userService.addNewUser(newUserRequest);

        NewEventDto newEventDto = setUpAddNewEvent();
        NewEventDto anotherEvent = setUpAddNewEvent();

        EventFullDto eventFullDto = eventService.addNewEvent(newEventDto, user.getId());
        eventService.addNewEvent(anotherEvent, anotherUser.getId());

        List<EventShortDto> result = eventService.getUserEvents(0, 10, user.getId());

        assertEquals(List.of(eventMapper.toEventShortDtoFromEventFullDto(eventFullDto)), result);
    }

    @Test
    void getUserEventsWhenUserIsNotFound() {
        // Проверка случая, когда пользователь не найден в базе данных

        NewEventDto newEventDto = setUpAddNewEvent();
        eventService.addNewEvent(newEventDto, user.getId());

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        eventService.getUserEvents(0, 10, 0L));

        assertEquals("Пользователь с id 0 не найден в базе данных", thrown.getMessage());
    }

    @Test
    void cancelEventFromUserWhenValidData() {
        // Проверка корректного сценария

        NewEventDto newEventDto = setUpAddNewEvent();
        EventFullDto eventFullDto = eventService.addNewEvent(newEventDto, user.getId());
        eventFullDto = eventService.cancelEventFromUser(user.getId(), eventFullDto.getId());

        assertEquals(State.CANCELED, eventFullDto.getState());
    }

    @Test
    void cancelEventWhenUserIsNotFoundThenReturnBadRequest() {
        // Проверка случая, когда пользователь не найден в базе данных

        NewEventDto newEventDto = setUpAddNewEvent();
        EventFullDto eventFullDto = eventService.addNewEvent(newEventDto, user.getId());

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        eventService.cancelEventFromUser(0L, eventFullDto.getId()));

        assertEquals("Пользователь с id 0 не найден в базе данных", thrown.getMessage());
    }

    @Test
    void cancelEventWhenUserNotIsInitiator() {
        // Проверка случая, когда пользователь не является инициатором события

        NewEventDto newEventDto = setUpAddNewEvent();
        EventFullDto eventFullDto = eventService.addNewEvent(newEventDto, user.getId());

        NewUserRequest newUserRequest = new NewUserRequest();
        newUserRequest.setEmail("Another@gmail.com");
        newUserRequest.setName("AnotherName");
        UserDto anotherUser = userService.addNewUser(newUserRequest);

        IllegalStateException thrown = Assertions
                .assertThrows(IllegalStateException.class, () ->
                        eventService.cancelEventFromUser(anotherUser.getId(), eventFullDto.getId()));

        assertEquals("Пользователь с id " + anotherUser.getId() + " не является инициатором события",
                thrown.getMessage());
    }

    @Test
    void publishEventWhenValidData() {
        // Проверка корректного сценария

        NewEventDto newEventDto = setUpAddNewEvent();
        EventFullDto eventFullDto = eventService.addNewEvent(newEventDto, user.getId());

        eventFullDto = eventService.publishEvent(eventFullDto.getId());

        assertEquals(State.PUBLISHED, eventFullDto.getState());
    }

    @Test
    void publishEventWhenEventNotFoundThenThrowException() {
        // Проверка случая, когда событие не найдено в базе данных

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        eventService.publishEvent(0L));

        assertEquals("Событие с id 0 не найдено в базе данных", thrown.getMessage());
    }

    @Test
    void rejectEventWhenValidData() {
        // Проверка корректного сценария
        NewEventDto newEventDto = setUpAddNewEvent();
        EventFullDto eventFullDto = eventService.addNewEvent(newEventDto, user.getId());

        eventFullDto = eventService.rejectEvent(eventFullDto.getId());

        assertEquals(State.CANCELED, eventFullDto.getState());
    }

    @Test
    void rejectEventWhenEventNotFound() {
        // Проверка случая, когда событие не найдено в базе данных
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        eventService.rejectEvent(0L));

        assertEquals("Событие с id 0 не найдено в базе данных", thrown.getMessage());
    }

    @Test
    void searchEvents() {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(categoryDto.getId());
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        newEventDto.setTitle("Test Event");

        LocationDto newLocation = new LocationDto();
        newLocation.setLat(44.0);
        newLocation.setLon(55.0);

        newEventDto.setLocation(newLocation);
        newEventDto.setTitle("012");
        newEventDto.setParticipantLimit(1);
        newEventDto.setPaid(false);

        NewEventDto anotherEventDto = new NewEventDto();
        anotherEventDto.setAnnotation("98765432109876543210");
        anotherEventDto.setDescription("98765432109876543210");
        anotherEventDto.setCategory(categoryDto.getId());
        anotherEventDto.setEventDate(LocalDateTime.now().plusMonths(10));
        anotherEventDto.setTitle("Another Event");

        LocationDto anotherLocation = new LocationDto();
        anotherLocation.setLat(144.0);
        anotherLocation.setLon(155.0);

        anotherEventDto.setLocation(anotherLocation);
        anotherEventDto.setTitle("210");
        anotherEventDto.setParticipantLimit(10);
        anotherEventDto.setPaid(true);

        EventFullDto eventFullDto = eventService.addNewEvent(newEventDto, user.getId());
        eventService.addNewEvent(anotherEventDto, user.getId());

        List<EventShortDto> result = eventService.searchEvents("1", new Long[]{categoryDto.getId()},
        false, null, null, false, null, 0, 10,
                "/events", "0:0:0:0:0:0:0:1", 44.0, 55.0);

        assertEquals(eventMapper.toEventShortDtoFromEventFullDto(eventFullDto), result.get(0));
    }

    @Test
    void getEventFullInfoByIdWhenValidArguments() {
        // Проверка корректного сценария
        NewEventDto newEventDto = setUpAddNewEvent();
        EventFullDto eventFullDto = eventService.addNewEvent(newEventDto, user.getId());

        EventFullDto result = eventService.getEventFullInfoById(eventFullDto.getId(),
                "0:0:0:0:0:0:0:1", "/events");

        assertEquals(eventFullDto, result);
    }

    @Test
    void getEventFullInfoByIdWhenEventNotFoundThenThrowException() {
        // Проверка случая, когда событие не найдено в базе данных

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        eventService.getEventFullInfoById(0L,
                                "0:0:0:0:0:0:0:1", "/events"));

        assertEquals("Событие с id 0 не найдено в базе данных", thrown.getMessage());
    }

    @Test
    void putEventByAdminWhenValidArguments() {
        // Проверка корректного сценария
        NewEventDto newEventDto = setUpAddNewEvent();
        EventFullDto eventFullDto = eventService.addNewEvent(newEventDto, user.getId());

        NewCategoryDto anotherCategory = new NewCategoryDto();
        anotherCategory.setName("Category by Admin");
        CategoryDto categoryByAdmin = categoryService.addNewCategory(anotherCategory);

        LocationDto newLocation = new LocationDto();
        newLocation.setLat(244.0);
        newLocation.setLon(255.0);
        LocationDto location = locationService.addLocation(newLocation);

        AdminUpdateEventRequest adminUpdateEventRequest = new AdminUpdateEventRequest();
        adminUpdateEventRequest.setAnnotation("Updated By Admin");
        adminUpdateEventRequest.setCategory(categoryByAdmin.getId());
        adminUpdateEventRequest.setDescription("Description By Admin");
        adminUpdateEventRequest.setEventDate(LocalDateTime.of(2023, 10, 2,11, 0));
        adminUpdateEventRequest.setLocation(LocationMapper.toLocationFromLocationDto(location));
        adminUpdateEventRequest.setPaid(true);
        adminUpdateEventRequest.setParticipantLimit(100);
        adminUpdateEventRequest.setRequestModeration(true);
        adminUpdateEventRequest.setTitle("Title By Admin");

        EventFullDto result = eventService.putEventByAdmin(eventFullDto.getId(), adminUpdateEventRequest);

        assertEquals(adminUpdateEventRequest.getAnnotation(), result.getAnnotation());
        assertEquals(adminUpdateEventRequest.getCategory(), result.getCategory().getId());
        assertEquals(adminUpdateEventRequest.getDescription(), result.getDescription());
        assertEquals(adminUpdateEventRequest.getEventDate(), result.getEventDate());
        assertEquals(LocationMapper.toLocationDtoFromLocation(adminUpdateEventRequest.getLocation()),
                result.getLocation());
        assertEquals(adminUpdateEventRequest.isPaid(), result.isPaid());
        assertEquals(adminUpdateEventRequest.getParticipantLimit(), adminUpdateEventRequest.getParticipantLimit());
        assertEquals(adminUpdateEventRequest.isRequestModeration(), result.isRequestModeration());
        assertEquals(adminUpdateEventRequest.getTitle(), result.getTitle());

    }

    @Test
    void putEventByAdminWhenEventNotFoundThenThrowException() {
        // Проверка случая, когда событие не найдено в базе данных
        NewCategoryDto anotherCategory = new NewCategoryDto();
        anotherCategory.setName("Category by Admin");
        CategoryDto categoryByAdmin = categoryService.addNewCategory(anotherCategory);

        LocationDto newLocation = new LocationDto();
        newLocation.setLat(244.0);
        newLocation.setLon(255.0);
        LocationDto location = locationService.addLocation(newLocation);

        AdminUpdateEventRequest adminUpdateEventRequest = new AdminUpdateEventRequest();
        adminUpdateEventRequest.setAnnotation("Updated By Admin");
        adminUpdateEventRequest.setCategory(categoryByAdmin.getId());
        adminUpdateEventRequest.setDescription("Description By Admin");
        adminUpdateEventRequest.setEventDate(LocalDateTime.of(2023, 10, 2,11, 0));
        adminUpdateEventRequest.setLocation(LocationMapper.toLocationFromLocationDto(location));
        adminUpdateEventRequest.setPaid(true);
        adminUpdateEventRequest.setParticipantLimit(100);
        adminUpdateEventRequest.setRequestModeration(true);
        adminUpdateEventRequest.setTitle("Title By Admin");

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        eventService.putEventByAdmin(0L, adminUpdateEventRequest));

        assertEquals("Событие с id 0 не найдено в базе данных", thrown.getMessage());
    }

    @Test
    void putEventByAdminWhenLocationNotFound() {
        // Случай, когда локация не найдена
        NewEventDto newEventDto = setUpAddNewEvent();
        EventFullDto eventFullDto = eventService.addNewEvent(newEventDto, user.getId());

        NewCategoryDto anotherCategory = new NewCategoryDto();
        anotherCategory.setName("Category by Admin");
        CategoryDto categoryByAdmin = categoryService.addNewCategory(anotherCategory);

        LocationDto newLocation = new LocationDto();
        newLocation.setId(0L);
        newLocation.setLat(244.0);
        newLocation.setLon(255.0);

        AdminUpdateEventRequest adminUpdateEventRequest = new AdminUpdateEventRequest();
        adminUpdateEventRequest.setAnnotation("Updated By Admin");
        adminUpdateEventRequest.setCategory(categoryByAdmin.getId());
        adminUpdateEventRequest.setDescription("Description By Admin");
        adminUpdateEventRequest.setEventDate(LocalDateTime.of(2023, 10, 2,11, 0));
        adminUpdateEventRequest.setLocation(LocationMapper.toLocationFromLocationDto(newLocation));
        adminUpdateEventRequest.setPaid(true);
        adminUpdateEventRequest.setParticipantLimit(100);
        adminUpdateEventRequest.setRequestModeration(true);
        adminUpdateEventRequest.setTitle("Title By Admin");


        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        eventService.putEventByAdmin(eventFullDto.getId(), adminUpdateEventRequest));

        assertEquals("Локация с id 0 не найдена в базе данных", thrown.getMessage());
    }

    @Test
    void putEventByAdminWhenCategoryNotFound() {
        // Проверка случая, когда категория не найдена в базе данных
        NewEventDto newEventDto = setUpAddNewEvent();
        EventFullDto eventFullDto = eventService.addNewEvent(newEventDto, user.getId());

        LocationDto newLocation = new LocationDto();
        newLocation.setLat(244.0);
        newLocation.setLon(255.0);
        LocationDto location = locationService.addLocation(newLocation);

        AdminUpdateEventRequest adminUpdateEventRequest = new AdminUpdateEventRequest();
        adminUpdateEventRequest.setAnnotation("Updated By Admin");
        adminUpdateEventRequest.setCategory(0L);
        adminUpdateEventRequest.setDescription("Description By Admin");
        adminUpdateEventRequest.setEventDate(LocalDateTime.of(2023, 10, 2,11, 0));
        adminUpdateEventRequest.setLocation(LocationMapper.toLocationFromLocationDto(location));
        adminUpdateEventRequest.setPaid(true);
        adminUpdateEventRequest.setParticipantLimit(100);
        adminUpdateEventRequest.setRequestModeration(true);
        adminUpdateEventRequest.setTitle("Title By Admin");

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        eventService.putEventByAdmin(eventFullDto.getId(), adminUpdateEventRequest));

        assertEquals("Категория с id 0 не найдена в базе данных", thrown.getMessage());
    }

    @Test
    void searchEventByAdmin() {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(categoryDto.getId());
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        newEventDto.setTitle("Test Event");

        LocationDto newLocation = new LocationDto();
        newLocation.setLat(44.0);
        newLocation.setLon(55.0);

        newEventDto.setLocation(newLocation);
        newEventDto.setTitle("012");
        newEventDto.setParticipantLimit(1);
        newEventDto.setPaid(false);

        NewEventDto anotherEventDto = new NewEventDto();
        anotherEventDto.setAnnotation("98765432109876543210");
        anotherEventDto.setDescription("98765432109876543210");
        anotherEventDto.setCategory(categoryDto.getId());
        anotherEventDto.setEventDate(LocalDateTime.now().plusMonths(10));
        anotherEventDto.setTitle("Another Event");

        LocationDto anotherLocation = new LocationDto();
        anotherLocation.setLat(144.0);
        anotherLocation.setLon(155.0);

        anotherEventDto.setLocation(anotherLocation);
        anotherEventDto.setTitle("210");
        anotherEventDto.setParticipantLimit(10);
        anotherEventDto.setPaid(true);

        EventFullDto eventFullDto = eventService.addNewEvent(newEventDto, user.getId());
        eventService.addNewEvent(anotherEventDto, user.getId());

        List<EventFullDto> result = eventService.searchEventByAdmin(new Long[]{user.getId()},
                new State[]{State.PENDING, State.PUBLISHED},
                new Long[]{categoryDto.getId()},
                null, null,  0, 10, 44.0, 55.0);

        assertEquals(eventFullDto, result.get(0));
    }

    @Test
    void searchEventInLocationWhenValidArguments() {
        // Проверка корректного сценария
        NewEventDto newEventDto = setUpAddNewEvent();
        EventFullDto eventFullDto = eventService.addNewEvent(newEventDto, user.getId());

        List<EventShortDto> result = eventService.searchEventInLocation(44.0, 55.0);

        assertEquals(1, result.size());
        assertEquals(eventMapper.toEventShortDtoFromEventFullDto(eventFullDto), result.get(0));
    }


    private NewEventDto setUpAddNewEvent() {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(categoryDto.getId());
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        newEventDto.setTitle("Test Event");

        LocationDto newLocation = new LocationDto();
        newLocation.setLat(44.0);
        newLocation.setLon(55.0);

        newEventDto.setLocation(newLocation);
        newEventDto.setTitle("012");
        newEventDto.setParticipantLimit(1);

        return newEventDto;
    }

    private UpdateEventRequest setUpPatchEvent() {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(categoryDto.getId());
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        newEventDto.setTitle("Test Event");

        newEventDto.setLocation(locationDto);
        newEventDto.setTitle("012");
        newEventDto.setParticipantLimit(1);

        EventFullDto savedEvent = eventService.addNewEvent(newEventDto, user.getId());

        UpdateEventRequest updateEventRequest = new UpdateEventRequest();
        updateEventRequest.setAnnotation("98765432109876543210");
        updateEventRequest.setCategory(categoryDto.getId());
        updateEventRequest.setDescription("98765432109876543210");
        updateEventRequest.setEventDate(LocalDateTime.now().plusMonths(3));
        updateEventRequest.setEventId(savedEvent.getId());
        updateEventRequest.setPaid(true);
        updateEventRequest.setParticipantLimit(10);
        updateEventRequest.setTitle("210");

        return updateEventRequest;
    }


}