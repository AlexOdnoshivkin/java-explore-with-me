package ru.practicum.ewmservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;
import ru.practicum.ewmservice.models.category.dto.CategoryDto;
import ru.practicum.ewmservice.models.event.dto.EventFullDto;
import ru.practicum.ewmservice.models.event.dto.EventShortDto;
import ru.practicum.ewmservice.models.event.dto.NewEventDto;
import ru.practicum.ewmservice.models.event.dto.UpdateEventRequest;
import ru.practicum.ewmservice.models.location.dto.LocationDto;
import ru.practicum.ewmservice.models.participation_request.Status;
import ru.practicum.ewmservice.models.participation_request.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.models.user.dto.UserShortDto;
import ru.practicum.ewmservice.services.EventService;
import ru.practicum.ewmservice.services.ParticipationRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(PrivateController.class)
class PrivateControllerTest {

    @MockBean
    private EventService eventService;

    @MockBean
    private ParticipationRequestService requestService;

    @Autowired
    private PrivateController controller;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void addNewEventWhenValidArguments() throws Exception {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(1L);
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        LocationDto locationDto = new LocationDto();
        locationDto.setId(1L);
        locationDto.setName("Test Location");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);
        newEventDto.setLocation(locationDto);
        newEventDto.setTitle("012");
        newEventDto.setParticipantLimit(1);

        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);
        eventFullDto.setAnnotation(newEventDto.getAnnotation());
        eventFullDto.setDescription(newEventDto.getDescription());
        eventFullDto.setCategory(new CategoryDto());
        eventFullDto.setEventDate(newEventDto.getEventDate());
        eventFullDto.setLocation(newEventDto.getLocation());
        eventFullDto.setTitle(newEventDto.getTitle());

        when(eventService.addNewEvent(any(), anyLong())).thenReturn(eventFullDto);

        mockMvc.perform(post("/users/1/events", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class));
    }

    @Test
    void addNewEventWhenAnnotationIsNullThenReturnBadRequest() throws Exception {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(1L);
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        LocationDto locationDto = new LocationDto();
        locationDto.setId(1L);
        locationDto.setName("Test Location");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);
        newEventDto.setLocation(locationDto);
        newEventDto.setTitle("012");

        mockMvc.perform(post("/users/1/events", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewEventWhenAnnotationSizeIs19ThenReturnBadRequest() throws Exception {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("0123456789012345678");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(1L);
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        LocationDto locationDto = new LocationDto();
        locationDto.setId(1L);
        locationDto.setName("Test Location");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);
        newEventDto.setLocation(locationDto);
        newEventDto.setTitle("012");

        mockMvc.perform(post("/users/1/events", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewEventWhenCategoryIsNullThenReturnBadRequest() throws Exception {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        LocationDto locationDto = new LocationDto();
        locationDto.setId(1L);
        locationDto.setName("Test Location");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);
        newEventDto.setLocation(locationDto);
        newEventDto.setTitle("012");

        mockMvc.perform(post("/users/1/events", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewEventWhenDescriptionIsNullThenReturnBadRequest() throws Exception {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setCategory(1L);
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        LocationDto locationDto = new LocationDto();
        locationDto.setId(1L);
        locationDto.setName("Test Location");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);
        newEventDto.setLocation(locationDto);
        newEventDto.setTitle("012");

        mockMvc.perform(post("/users/1/events", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewEventWhenDescriptionSizeIS19ThenReturnBadRequest() throws Exception {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("0123456789012345678");
        newEventDto.setCategory(1L);
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        LocationDto locationDto = new LocationDto();
        locationDto.setId(1L);
        locationDto.setName("Test Location");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);
        newEventDto.setLocation(locationDto);
        newEventDto.setTitle("012");

        mockMvc.perform(post("/users/1/events", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewEventWhenEventDateIsNullThenReturnBadRequest() throws Exception {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(1L);
        LocationDto locationDto = new LocationDto();
        locationDto.setId(1L);
        locationDto.setName("Test Location");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);
        newEventDto.setLocation(locationDto);
        newEventDto.setTitle("012");

        mockMvc.perform(post("/users/1/events", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewEventWhenDateIsBeforeThenReturnBadRequest() throws Exception {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(1L);
        newEventDto.setEventDate(LocalDateTime.now());
        LocationDto locationDto = new LocationDto();
        locationDto.setId(1L);
        locationDto.setName("Test Location");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);
        newEventDto.setLocation(locationDto);
        newEventDto.setTitle("012");

        mockMvc.perform(post("/users/1/events", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewEventWhenLocationIsNullThenReturnBadRequest() throws Exception {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(1L);
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        newEventDto.setTitle("012");

        mockMvc.perform(post("/users/1/events", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewEventWhenParticipantLimitIsNegativeThenReturnBadRequest() throws Exception {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(1L);
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        LocationDto locationDto = new LocationDto();
        locationDto.setId(1L);
        locationDto.setName("Test Location");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);
        newEventDto.setLocation(locationDto);
        newEventDto.setTitle("012");
        newEventDto.setParticipantLimit(-1);

        mockMvc.perform(post("/users/1/events", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewEventWhenTitleIsNullThenReturnBadRequest() throws Exception {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(1L);
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        LocationDto locationDto = new LocationDto();
        locationDto.setId(1L);
        locationDto.setName("Test Location");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);
        newEventDto.setLocation(locationDto);
        newEventDto.setParticipantLimit(1);

        mockMvc.perform(post("/users/1/events", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewEventWhenTitleSizeIs2ThenReturnBadRequest() throws Exception {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("01234567890123456789");
        newEventDto.setDescription("01234567890123456789");
        newEventDto.setCategory(1L);
        newEventDto.setEventDate(LocalDateTime.now().plusMonths(2));
        LocationDto locationDto = new LocationDto();
        locationDto.setId(1L);
        locationDto.setName("Test Location");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);
        newEventDto.setLocation(locationDto);
        newEventDto.setTitle("01");
        newEventDto.setParticipantLimit(1);

        mockMvc.perform(post("/users/1/events", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewParticipationRequestWhenValidArguments() throws Exception {
        ParticipationRequestDto participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setRequester(1L);
        participationRequestDto.setId(1L);
        participationRequestDto.setStatus(Status.CONFIRMED);
        participationRequestDto.setEvent(1L);

        when(requestService.addNewParticipationRequest(anyLong(), anyLong())).thenReturn(participationRequestDto);

        mockMvc.perform(post("/users/1/requests?eventId=1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class));

    }

    @Test
    void patchEventWhenValidArguments() throws Exception {
        UpdateEventRequest updateEventRequest = new UpdateEventRequest();
        updateEventRequest.setAnnotation("01234567890123456789");
        updateEventRequest.setCategory(1L);
        updateEventRequest.setDescription("01234567890123456789");
        updateEventRequest.setEventDate(LocalDateTime.now().plusMonths(2));
        updateEventRequest.setEventId(1L);
        updateEventRequest.setPaid(false);
        updateEventRequest.setParticipantLimit(10);
        updateEventRequest.setTitle("012");

        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);

        when(eventService.patchEvent(any(), anyLong())).thenReturn(eventFullDto);

        mockMvc.perform(patch("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class));
    }

    @Test
    void patchEventWhenAnnotationSizeIS19ThenReturnBadRequest() throws Exception {
        UpdateEventRequest updateEventRequest = new UpdateEventRequest();
        updateEventRequest.setAnnotation("0123456789012345678");
        updateEventRequest.setCategory(1L);
        updateEventRequest.setDescription("01234567890123456789");
        updateEventRequest.setEventDate(LocalDateTime.now().plusMonths(2));
        updateEventRequest.setEventId(1L);
        updateEventRequest.setPaid(false);
        updateEventRequest.setParticipantLimit(10);
        updateEventRequest.setTitle("012");

        mockMvc.perform(patch("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchEventWhenDescriptionSizeIS19ThenReturnBadRequest() throws Exception {
        UpdateEventRequest updateEventRequest = new UpdateEventRequest();
        updateEventRequest.setAnnotation("01234567890123456789");
        updateEventRequest.setCategory(1L);
        updateEventRequest.setDescription("0123456789012345678");
        updateEventRequest.setEventDate(LocalDateTime.now().plusMonths(2));
        updateEventRequest.setEventId(1L);
        updateEventRequest.setPaid(false);
        updateEventRequest.setParticipantLimit(10);
        updateEventRequest.setTitle("012");

        mockMvc.perform(patch("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchEventWhenEventDateIsBeforeThenReturnBadRequest() throws Exception {
        UpdateEventRequest updateEventRequest = new UpdateEventRequest();
        updateEventRequest.setAnnotation("01234567890123456789");
        updateEventRequest.setCategory(1L);
        updateEventRequest.setDescription("01234567890123456789");
        updateEventRequest.setEventDate(LocalDateTime.now());
        updateEventRequest.setEventId(1L);
        updateEventRequest.setPaid(false);
        updateEventRequest.setParticipantLimit(10);
        updateEventRequest.setTitle("012");

        mockMvc.perform(patch("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchEventWhenEventIdIsNullThenReturnBadRequest() throws Exception {
        UpdateEventRequest updateEventRequest = new UpdateEventRequest();
        updateEventRequest.setAnnotation("01234567890123456789");
        updateEventRequest.setCategory(1L);
        updateEventRequest.setDescription("01234567890123456789");
        updateEventRequest.setEventDate(LocalDateTime.now().plusMonths(2));
        updateEventRequest.setPaid(false);
        updateEventRequest.setParticipantLimit(10);
        updateEventRequest.setTitle("012");

        mockMvc.perform(patch("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchEventWhenTitleSizeIs2ThenReturnBadRequest() throws Exception {
        UpdateEventRequest updateEventRequest = new UpdateEventRequest();
        updateEventRequest.setAnnotation("01234567890123456789");
        updateEventRequest.setCategory(1L);
        updateEventRequest.setDescription("01234567890123456789");
        updateEventRequest.setEventDate(LocalDateTime.now().plusMonths(2));
        updateEventRequest.setEventId(1L);
        updateEventRequest.setPaid(false);
        updateEventRequest.setParticipantLimit(10);
        updateEventRequest.setTitle("01");

        mockMvc.perform(patch("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelParticipationRequestByUser() throws Exception {
        ParticipationRequestDto participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setId(1L);
        participationRequestDto.setCreated(LocalDateTime.now());
        participationRequestDto.setEvent(1L);
        participationRequestDto.setRequester(1L);
        participationRequestDto.setStatus(Status.CANCELED);

        when(requestService.cancelParticipationRequestByUser(anyLong(), anyLong())).thenReturn(participationRequestDto);

        mockMvc.perform(patch("/users/1/requests/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class));
    }

    @Test
    void confirmParticipationRequest() throws Exception {
        ParticipationRequestDto participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setId(1L);
        participationRequestDto.setCreated(LocalDateTime.now());
        participationRequestDto.setEvent(1L);
        participationRequestDto.setRequester(1L);
        participationRequestDto.setStatus(Status.CONFIRMED);

        when(requestService.confirmParticipationRequest(anyLong(), anyLong(), anyLong()))
                .thenReturn(participationRequestDto);

        mockMvc.perform(patch("/users/1/events/1/requests/1/confirm")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class));
    }

    @Test
    void rejectParticipationRequest() throws Exception {
        ParticipationRequestDto participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setId(1L);
        participationRequestDto.setCreated(LocalDateTime.now());
        participationRequestDto.setEvent(1L);
        participationRequestDto.setRequester(1L);
        participationRequestDto.setStatus(Status.REJECTED);

        when(requestService.rejectParticipationRequest(anyLong(), anyLong(), anyLong()))
                .thenReturn(participationRequestDto);

        mockMvc.perform(patch("/users/1/events/1/requests/1/reject")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class));
    }

    @Test
    void getUserEventsWhenValidArguments() throws Exception {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setAnnotation("01234567890123456789");
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("TestCategory");
        eventShortDto.setCategory(categoryDto);
        eventShortDto.setEventDate(LocalDateTime.now().plusMonths(2));
        eventShortDto.setId(1L);
        UserShortDto userShortDto = new UserShortDto();
        userShortDto.setId(1L);
        userShortDto.setName("Test User");
        eventShortDto.setInitiator(userShortDto);
        eventShortDto.setPaid(false);
        eventShortDto.setTitle("012");


        when(eventService.getUserEvents(anyInt(), anyInt(), anyLong())).thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1L), Long.class));
    }

    @Test
    void getUserEventsWhenFromIsNegativeThenReturnBadRequest() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/users/1/events")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("from", "-1")
                                        .param("size", "10"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: getUserEvents.from: must be greater " +
                        "than or equal to 0",
                thrown.getMessage());
    }

    @Test
    void getUserEventsWhenSizeIsZeroThenReturnBadRequest() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/users/1/events")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("from", "0")
                                        .param("size", "0"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: getUserEvents.size: must be greater than 0",
                thrown.getMessage());
    }

    @Test
    void getUserEvent() throws Exception {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setAnnotation("01234567890123456789");
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("TestCategory");
        eventFullDto.setCategory(categoryDto);
        eventFullDto.setEventDate(LocalDateTime.now().plusMonths(2));
        eventFullDto.setId(1L);
        UserShortDto userShortDto = new UserShortDto();
        userShortDto.setId(1L);
        userShortDto.setName("Test User");
        eventFullDto.setInitiator(userShortDto);
        eventFullDto.setPaid(false);
        eventFullDto.setTitle("012");

        when(eventService.getUserEventFullInfo(anyLong(), anyLong())).thenReturn(eventFullDto);

        mockMvc.perform(get("/users/1/events/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class));
    }

    @Test
    void getParticipationRequestsByUser() throws Exception {
        ParticipationRequestDto participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setId(1L);
        participationRequestDto.setCreated(LocalDateTime.now());
        participationRequestDto.setEvent(1L);
        participationRequestDto.setRequester(2L);
        participationRequestDto.setStatus(Status.PENDING);

        when(requestService.getUserParticipationRequests(anyLong())).thenReturn(List.of(participationRequestDto));

        mockMvc.perform(get("/users/1/requests")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1L), Long.class));
    }

    @Test
    void getRequestsByUserEvent() throws Exception {
        ParticipationRequestDto participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setId(1L);
        participationRequestDto.setCreated(LocalDateTime.now());
        participationRequestDto.setEvent(1L);
        participationRequestDto.setRequester(2L);
        participationRequestDto.setStatus(Status.PENDING);

        when(requestService.getUserEventParticipationRequests(anyLong(), anyLong()))
                .thenReturn(List.of(participationRequestDto));

        mockMvc.perform(get("/users/1/events/1/requests")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1L), Long.class));
    }


}