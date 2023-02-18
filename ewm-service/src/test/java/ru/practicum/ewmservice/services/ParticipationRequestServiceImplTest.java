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
import ru.practicum.ewmservice.models.event.dto.EventFullDto;
import ru.practicum.ewmservice.models.event.dto.NewEventDto;
import ru.practicum.ewmservice.models.event.dto.UpdateEventRequest;
import ru.practicum.ewmservice.models.location.dto.LocationDto;
import ru.practicum.ewmservice.models.participation_request.Status;
import ru.practicum.ewmservice.models.participation_request.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.models.user.dto.NewUserRequest;
import ru.practicum.ewmservice.models.user.dto.UserDto;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ParticipationRequestServiceImplTest {
    private final EntityManager em;

    private final ParticipationRequestService participationRequestService;

    private final EventService eventService;

    private final UserService userService;

    private final CategoryService categoryService;

    private EventFullDto event;

    private UserDto user;

    private UserDto initiator;

    @AfterEach
    void afterEach() {
        em.createNativeQuery("truncate table participation_requests");
    }

    @BeforeEach
    void setUp() {
        NewUserRequest newUser = new NewUserRequest();
        newUser.setName("Boris");
        newUser.setEmail("BorisBritva@gmail.com");
        user = userService.addNewUser(newUser);

        NewUserRequest another = new NewUserRequest();
        another.setName("Initiator");
        another.setEmail("init@gmail.com");
        initiator = userService.addNewUser(another);

        NewCategoryDto newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Test Category");
        CategoryDto categoryDto = categoryService.addNewCategory(newCategoryDto);

        LocationDto locationDto = new LocationDto();
        locationDto.setName("Test Location");
        locationDto.setLat(100.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);

        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(categoryDto.getId());
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));

        newEventDto.setLocation(locationDto);
        newEventDto.setTitle("012");
        newEventDto.setParticipantLimit(10);

        event = eventService.addNewEvent(newEventDto, initiator.getId());
    }

    @Test
    void addNewParticipationRequestWhenValidData() {
        // Проверка корректного сценария
        ParticipationRequestDto participationRequestDto =
                participationRequestService.addNewParticipationRequest(user.getId(), event.getId());

        assertEquals(user.getId(), participationRequestDto.getRequester());
        assertEquals(event.getId(), participationRequestDto.getEvent());
        assertEquals(Status.CONFIRMED, participationRequestDto.getStatus());
        assertEquals(1, eventService.getEventFullInfoById(event.getId(), null, null).getConfirmedRequests());
    }

    @Test
    void addNewParticipationRequestWhenUserIsNotFoundThenThrowException() {
        // Проверка случая, когда пользователь не найден в базе данных
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        participationRequestService.addNewParticipationRequest(0L, event.getId()));

        assertEquals("Пользователь с id 0 не найден в базе данных", thrown.getMessage());
    }

    @Test
    void addNewParticipationRequestWhenEventIsNotFoundThenThrowException() {
        // Проверка случая, когда пользователь не найден в базе данных
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        participationRequestService.addNewParticipationRequest(user.getId(), 0L));

        assertEquals("Событие с id 0 не найдено в базе данных", thrown.getMessage());
    }

    @Test
    void addNewParticipationRequestWhenRequesterIsInitiatorThenThrowException() {
        // Проверка случая, когда автор запроса является инициатором события
        IllegalStateException thrown = Assertions
                .assertThrows(IllegalStateException.class, () ->
                        participationRequestService.addNewParticipationRequest(initiator.getId(), event.getId()));

        assertEquals("Ининциатор не может ооставить заявку на участие в своём событии", thrown.getMessage());
    }

    @Test
    void addNewParticipationRequestWhenParticipationLimitIsExceededThenThrowException() {
        // Проверка случая, когда превышен лимит заявок в событии
        UpdateEventRequest updateEventRequest = new UpdateEventRequest();
        updateEventRequest.setEventId(event.getId());
        updateEventRequest.setParticipantLimit(0);
        eventService.patchEvent(updateEventRequest, initiator.getId());

        IllegalStateException thrown = Assertions
                .assertThrows(IllegalStateException.class, () ->
                        participationRequestService.addNewParticipationRequest(user.getId(), event.getId()));

        assertEquals("Превышен лимит заявок на участие в событии", thrown.getMessage());
    }

    @Test
    void addNewParticipationRequestWhenRequestIsAlreadyExistThenThrowException() {
        // Проверка случая, когда запрос пользователя к этому событию уже существует
        participationRequestService.addNewParticipationRequest(user.getId(), event.getId());

        IllegalStateException thrown = Assertions
                .assertThrows(IllegalStateException.class, () ->
                        participationRequestService.addNewParticipationRequest(user.getId(), event.getId()));

        assertEquals("Запрос уже существует", thrown.getMessage());
    }

    @Test
    void getUserParticipationRequestsWhenValidData() {
        // Проверка корректного сценария
        ParticipationRequestDto participationRequestDto = participationRequestService
                .addNewParticipationRequest(user.getId(), event.getId());

        List<ParticipationRequestDto> result = participationRequestService.getUserParticipationRequests(user.getId());

        assertEquals(1, result.size());
        assertEquals(participationRequestDto, result.get(0));
    }

    @Test
    void getUserParticipationRequestsWhenUserIsNotFoundThenThrowException() {
        // Проверка случая, когда пользователь не найден в базе данных
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        participationRequestService.getUserParticipationRequests(0L));

        assertEquals("Пользователь с id 0 не найден в базе данных", thrown.getMessage());
    }

    @Test
    void getUserEventParticipationRequestsWhenValidData() {
        // Проверка корректного сценария
        ParticipationRequestDto participationRequestDto = participationRequestService
                .addNewParticipationRequest(user.getId(), event.getId());

        List<ParticipationRequestDto> result =
                participationRequestService.getUserEventParticipationRequests(initiator.getId(), event.getId());

        assertEquals(1, result.size());
        assertEquals(participationRequestDto, result.get(0));
    }

    @Test
    void getUserEventParticipationRequestsWhenUserNotFoundThenThrowException() {
        // Проверка случая, когда пользователь не найден в базе данных
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        participationRequestService.getUserEventParticipationRequests(0L, event.getId()));

        assertEquals("Пользователь с id 0 не найден в базе данных", thrown.getMessage());
    }

    @Test
    void getUserEventParticipationRequestsWhenEventNotFoundThenThrowException() {
        // Проверка случая, когда событие не найдено в базе данных
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        participationRequestService.getUserEventParticipationRequests(initiator.getId(), 0L));

        assertEquals("Событие с id 0 не найдено в базе данных", thrown.getMessage());
    }

    @Test
    void cancelParticipationRequestByUserWhenValidData() {
        // Проверка корректного сценария
        ParticipationRequestDto participationRequestDto = participationRequestService
                .addNewParticipationRequest(user.getId(), event.getId());

        ParticipationRequestDto result = participationRequestService
                .cancelParticipationRequestByUser(user.getId(), participationRequestDto.getId());

        assertEquals(Status.CANCELED, result.getStatus());
    }

    @Test
    void cancelParticipationRequestByUserWhenUserNotFoundThenThrowException() {
        // Проверка случая, когда пользователь не найден в базе данных
        ParticipationRequestDto participationRequestDto = participationRequestService
                .addNewParticipationRequest(user.getId(), event.getId());

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        participationRequestService
                                .cancelParticipationRequestByUser(0L, participationRequestDto.getId()));

        assertEquals("Пользователь с id 0 не найден в базе данных", thrown.getMessage());
    }

    @Test
    void cancelParticipationRequestByUserWhenRequestNotFoundThenThrowException() {
        // Проверка случая, когда запрос на участие в событии не найден в базе данных
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        participationRequestService
                                .cancelParticipationRequestByUser(user.getId(), 0L));

        assertEquals("Запрос на участие не найден", thrown.getMessage());
    }

    @Test
    void cancelParticipationRequestByUserWhenUserIsNotRequesterThenThrowException() {
        // Проверка случая, когда пользователь не является автором запроса
        NewUserRequest anotherUser = new NewUserRequest();
        anotherUser.setName("Another Initiator");
        anotherUser.setEmail("secondInt@gmail.com");
        UserDto anotherRequester = userService.addNewUser(anotherUser);

        ParticipationRequestDto participationRequestDto = participationRequestService
                .addNewParticipationRequest(user.getId(), event.getId());

        IllegalStateException thrown = Assertions
                .assertThrows(IllegalStateException.class, () ->
                        participationRequestService
                                .cancelParticipationRequestByUser(anotherRequester.getId(),
                                        participationRequestDto.getId()));

        assertEquals("Пользователь не является автором запроса", thrown.getMessage());
    }

    @Test
    void confirmParticipationRequestWhenValidData() {
        // Проверка корректного сценария
        ParticipationRequestDto participationRequestDto = participationRequestService
                .addNewParticipationRequest(user.getId(), event.getId());

        ParticipationRequestDto result = participationRequestService
                .confirmParticipationRequest(user.getId(), event.getId(), participationRequestDto.getId());

        assertEquals(Status.CONFIRMED, result.getStatus());
    }

    @Test
    void confirmParticipationRequestWhenUserIsNotFoundThenThrowException() {
        // Проверка случая, когда пользователь не найден в базе данных
        ParticipationRequestDto participationRequestDto = participationRequestService
                .addNewParticipationRequest(user.getId(), event.getId());

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        participationRequestService
                                .confirmParticipationRequest(0L, event.getId(), participationRequestDto.getId()));

        assertEquals("Пользователь с id 0 не найден в базе данных", thrown.getMessage());
    }

    @Test
    void confirmParticipationRequestWhenEventIsNotFoundThenThrowException() {
        // Проверка случая, когда событие не найдено в базе данных
        ParticipationRequestDto participationRequestDto = participationRequestService
                .addNewParticipationRequest(user.getId(), event.getId());

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        participationRequestService
                                .confirmParticipationRequest(user.getId(), 0L, participationRequestDto.getId()));

        assertEquals("Событие с id 0 не найдено в базе данных", thrown.getMessage());
    }

    @Test
    void confirmParticipationRequestWhenRequestIsNotFoundThenThrowException() {
        // Проверка случая, когда запрос на участие в событии не найден
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        participationRequestService
                                .confirmParticipationRequest(user.getId(), event.getId(), 0L));

        assertEquals("Заявка на участие с id 0 не найдена в базе данных", thrown.getMessage());
    }

    @Test
    void rejectParticipationRequestWhenValidData() {
        // Проверка корректного сценария
        ParticipationRequestDto participationRequestDto = participationRequestService
                .addNewParticipationRequest(user.getId(), event.getId());

        ParticipationRequestDto result = participationRequestService
                .rejectParticipationRequest(user.getId(), event.getId(), participationRequestDto.getId());

        assertEquals(Status.REJECTED, result.getStatus());
    }

    @Test
    void rejectParticipationRequestWhenUserNotFoundThenThrowException() {
        // Проверка случая, когда пользователь не найден в базе данных
        ParticipationRequestDto participationRequestDto = participationRequestService
                .addNewParticipationRequest(user.getId(), event.getId());

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        participationRequestService
                                .rejectParticipationRequest(0L, event.getId(), participationRequestDto.getId()));

        assertEquals("Пользователь с id 0 не найден в базе данных", thrown.getMessage());
    }

    @Test
    void rejectParticipationRequestWhenEventNotFoundThenThrowException() {
        // Проверка случая, когда событие не найдено в базе данных
        ParticipationRequestDto participationRequestDto = participationRequestService
                .addNewParticipationRequest(user.getId(), event.getId());

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        participationRequestService
                                .rejectParticipationRequest(user.getId(), 0L, participationRequestDto.getId()));

        assertEquals("Событие с id 0 не найдено в базе данных", thrown.getMessage());
    }

    @Test
    void rejectParticipationRequestWhenRequestIsNotFoundThenThrowException() {
        // Проверка случая, когда запрос на участие в событии не найден
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        participationRequestService
                                .rejectParticipationRequest(user.getId(), event.getId(), 0L));

        assertEquals("Заявка на участие с id 0 не найдена в базе данных", thrown.getMessage());
    }

}