package ru.practicum.ewmservice.controllers;

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
import ru.practicum.ewmservice.models.compilation.dto.CompilationDto;
import ru.practicum.ewmservice.models.event.dto.EventFullDto;
import ru.practicum.ewmservice.models.event.dto.EventShortDto;
import ru.practicum.ewmservice.models.location.dto.LocationDto;
import ru.practicum.ewmservice.models.user.dto.UserShortDto;
import ru.practicum.ewmservice.services.CategoryService;
import ru.practicum.ewmservice.services.CompilationService;
import ru.practicum.ewmservice.services.EventService;
import ru.practicum.ewmservice.services.LocationService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(PublicController.class)
class PublicControllerTest {
    @MockBean
    private EventService eventService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private CompilationService compilationService;

    @MockBean
    private LocationService locationService;

    @Autowired
    private PublicController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void getEventFullInfoById() throws Exception {
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

        when(eventService.getEventFullInfoById(anyLong(), anyString(), anyString())).thenReturn(eventFullDto);

        mockMvc.perform(get("/events/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class));
    }

    @Test
    void searchEventsWhenValidArguments() throws Exception {
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

        when(eventService.searchEvents(anyString(), any(), anyBoolean(), any(), any(), anyBoolean(), anyString(),
                anyInt(), anyInt(), anyString(), anyString(), anyDouble(), anyDouble()))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("text", "TestText")
                        .param("categories", "1")
                        .param("paid", "false")
                        .param("rangeStart", "2023-01-06 13:30:38")
                        .param("rangeEnd", "2023-01-07 13:30:38")
                        .param("onlyAvailable", "false")
                        .param("sort", "EVENT_DATE")
                        .param("from", "0")
                        .param("size", "10")
                        .param("lat", "10.0")
                        .param("lon", "10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1L), Long.class));
    }

    @Test
    void searchEventsWhenFromIsNegativeThenReturnBadRequest() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/events")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("text", "TestText")
                                        .param("categories", "1")
                                        .param("paid", "false")
                                        .param("rangeStart", "2023-01-06 13:30:38")
                                        .param("rangeEnd", "2023-01-07 13:30:38")
                                        .param("onlyAvailable", "false")
                                        .param("sort", "EVENT_DATE")
                                        .param("from", "-1")
                                        .param("size", "10")
                                        .param("lat", "10.0")
                                        .param("lon", "10.0"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: searchEvents.from: " +
                        "must be greater than or equal to 0",
                thrown.getMessage());
    }

    @Test
    void searchEventsWhenSizeIsZeroThenReturnBadRequest() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/events")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("text", "TestText")
                                        .param("categories", "1")
                                        .param("paid", "false")
                                        .param("rangeStart", "2023-01-06 13:30:38")
                                        .param("rangeEnd", "2023-01-07 13:30:38")
                                        .param("onlyAvailable", "false")
                                        .param("sort", "EVENT_DATE")
                                        .param("from", "0")
                                        .param("size", "0")
                                        .param("lat", "10.0")
                                        .param("lon", "10.0"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: searchEvents.size: must be greater than 0",
                thrown.getMessage());
    }

    @Test
    void searchEventsWhenLatIsNegativeThenReturnBadRequest() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/events")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("text", "TestText")
                                        .param("categories", "1")
                                        .param("paid", "false")
                                        .param("rangeStart", "2023-01-06 13:30:38")
                                        .param("rangeEnd", "2023-01-07 13:30:38")
                                        .param("onlyAvailable", "false")
                                        .param("sort", "EVENT_DATE")
                                        .param("from", "0")
                                        .param("size", "10")
                                        .param("lat", "-10.0")
                                        .param("lon", "10.0"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: searchEvents.lat: " +
                        "must be greater than or equal to 0",
                thrown.getMessage());
    }

    @Test
    void searchEventsWhenLonIsNegativeThenReturnBadRequest() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/events")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("text", "TestText")
                                        .param("categories", "1")
                                        .param("paid", "false")
                                        .param("rangeStart", "2023-01-06 13:30:38")
                                        .param("rangeEnd", "2023-01-07 13:30:38")
                                        .param("onlyAvailable", "false")
                                        .param("sort", "EVENT_DATE")
                                        .param("from", "0")
                                        .param("size", "10")
                                        .param("lat", "10.0")
                                        .param("lon", "-10.0"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: searchEvents.lon: " +
                        "must be greater than or equal to 0",
                thrown.getMessage());
    }

    @Test
    void searchCompilationsWhenValidArguments() throws Exception {
        CompilationDto compilationDto = new CompilationDto();

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

        compilationDto.setId(1L);
        compilationDto.setTitle("Test Compilation");
        compilationDto.setPinned(true);
        compilationDto.setEvents(List.of(eventShortDto));

        when(compilationService.searchCompilations(anyBoolean(), anyInt(), anyInt()))
                .thenReturn(List.of(compilationDto));

        mockMvc.perform(get("/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1L), Long.class));
    }

    @Test
    void searchCompilationWhenFromIsNegative() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/compilations")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("pinned", "true")
                                        .param("from", "-1")
                                        .param("size", "10"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: searchCompilations.from: " +
                        "must be greater than or equal to 0",
                thrown.getMessage());
    }

    @Test
    void searchCompilationWhenSizeIsZeroThenReturnBadRequest() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/compilations")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("pinned", "true")
                                        .param("from", "0")
                                        .param("size", "0"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: searchCompilations.size: " +
                        "must be greater than 0",
                thrown.getMessage());
    }

    @Test
    void getCompilationById() throws Exception {
        CompilationDto compilationDto = new CompilationDto();

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

        compilationDto.setId(1L);
        compilationDto.setTitle("Test Compilation");
        compilationDto.setPinned(true);
        compilationDto.setEvents(List.of(eventShortDto));

        when(compilationService.getCompilationById(anyLong())).thenReturn(compilationDto);

        mockMvc.perform(get("/compilations/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class));
    }

    @Test
    void getCategoriesWhenValidArguments() throws Exception {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("TestCategory");

        when(categoryService.getCategories(anyInt(), anyInt())).thenReturn(List.of(categoryDto));

        mockMvc.perform(get("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1L), Long.class));
    }

    @Test
    void getCategoriesWhenFromIsNegativeThenReturnBadRequest() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/categories")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("from", "-1")
                                        .param("size", "10"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: getCategories.from: " +
                        "must be greater than or equal to 0",
                thrown.getMessage());
    }

    @Test
    void getCategoriesWhenSizeIsZeroThenReturnBadRequest() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/categories")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("from", "0")
                                        .param("size", "0"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: getCategories.size: must be greater than 0",
                thrown.getMessage());
    }

    @Test
    void getCategoryByIdWhenValidArguments() throws Exception {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("TestCategory");

        when(categoryService.getCategory(anyLong())).thenReturn(categoryDto);

        mockMvc.perform(get("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class));
    }

    @Test
    void getAllLocationsWhenValidArguments() throws Exception {
        LocationDto locationDto = new LocationDto();
        locationDto.setName("Test Location");
        locationDto.setId(1L);
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);
        locationDto.setRadius(10.0);

        when(locationService.getAllLocations(anyInt(), anyInt())).thenReturn(List.of(locationDto));

        mockMvc.perform(get("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1L), Long.class));
    }

    @Test
    void getAllLocationsWhenFromIsNegativeReturnBadRequest() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/locations")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("from", "-1")
                                        .param("size", "10"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: getAllLocations.from: " +
                        "must be greater than or equal to 0",
                thrown.getMessage());
    }

    @Test
    void getAllLocationsWhenSizeIsZeroThenReturnBadRequest() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/locations")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("from", "0")
                                        .param("size", "0"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: getAllLocations.size: must be greater than 0",
                thrown.getMessage());
    }

    @Test
    void searchEventsInLocationWhenValidArguments() throws Exception {
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

        when(eventService.searchEventInLocation(anyDouble(), anyDouble())).thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/locations/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("lat", "10.0")
                        .param("lon", "10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1L), Long.class));
    }

    @Test
    void searchEventsInLocationWhenLatIsNullThenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/locations/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("lon", "10.0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchEventsInLocationWhenLonIsNullThenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/locations/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("lat", "10.0"))
                .andExpect(status().isBadRequest());
    }



}