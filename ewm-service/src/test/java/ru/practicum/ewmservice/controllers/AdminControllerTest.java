package ru.practicum.ewmservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.practicum.ewmservice.models.category.dto.NewCategoryDto;
import ru.practicum.ewmservice.models.compilation.dto.CompilationDto;
import ru.practicum.ewmservice.models.compilation.dto.NewCompilationDto;
import ru.practicum.ewmservice.models.event.dto.AdminUpdateEventRequest;
import ru.practicum.ewmservice.models.event.dto.EventFullDto;
import ru.practicum.ewmservice.models.location.Location;
import ru.practicum.ewmservice.models.location.dto.LocationDto;
import ru.practicum.ewmservice.models.user.dto.NewUserRequest;
import ru.practicum.ewmservice.models.user.dto.UserDto;
import ru.practicum.ewmservice.services.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private CompilationService compilationService;

    @MockBean
    private EventService eventService;

    @MockBean
    private LocationService locationService;

    @Autowired
    private AdminController controller;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void addNewUserWhenValidData() throws Exception {
        NewUserRequest newUser = new NewUserRequest();
        newUser.setName("Boris");
        newUser.setEmail("BorisBritva@gmail.com");

        UserDto userDto = new UserDto();
        userDto.setName(newUser.getName());

        when(userService.addNewUser(any())).thenReturn(userDto);

        mockMvc.perform(post("/admin/users", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(userDto.getName()), String.class));

    }

    @Test
    void addNewUserWhenNameIsEmptyThenReturnBadRequest() throws Exception {
        NewUserRequest newUser = new NewUserRequest();
        newUser.setName("");
        newUser.setEmail("BorisBritva@gmail.com");

        mockMvc.perform(post("/admin/users", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewUserWhenEmailIsEmptyThenReturnBadRequest() throws Exception {
        NewUserRequest newUser = new NewUserRequest();
        newUser.setName("Boris");
        newUser.setEmail("");

        mockMvc.perform(post("/admin/users", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewUserWhenWrongEmailStructureThenReturnBadRequest() throws Exception {
        NewUserRequest newUser = new NewUserRequest();
        newUser.setName("Boris");
        newUser.setEmail("Borismail");

        mockMvc.perform(post("/admin/users", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsersWhenValidData() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("TestEmail");
        userDto.setName("TestName");
        List<UserDto> users = List.of(userDto);

        when(userService.getUsers(anyInt(), anyInt(), any())).thenReturn(users);

        mockMvc.perform(get("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto))
                        .param("ids", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDto.getName()), String.class))
                .andExpect(jsonPath("$[0].email", is(userDto.getEmail()), String.class));
    }

    @Test
    void getUserWhenFromIsNegative() {

        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/admin/users")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("from", "-10")
                                        .param("ids", "1"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: getUsers.from: must be greater than or equal to 0",
                thrown.getMessage());
    }

    @Test
    void getUserWhenSizeIsNegative() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/admin/users")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("size", "-10")
                                        .param("ids", "1"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: getUsers.size: must be greater than 0",
                thrown.getMessage());
    }

    @Test
    void deleteUser() throws Exception {
        doNothing().when(userService).deleteUser(anyLong());

        mockMvc.perform(delete("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void addNewCategoryWhenValidArguments() throws Exception {
        NewCategoryDto newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("TestName");

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(newCategoryDto.getName());
        categoryDto.setId(1L);

        when(categoryService.addNewCategory(any())).thenReturn(categoryDto);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.name", is(newCategoryDto.getName()), String.class));
    }

    @Test
    void addNewCategoryWhenNameIsEmptyThenReturnBadRequest() throws Exception {
        NewCategoryDto newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("");


        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isBadRequest());

    }

    @Test
    void patchCategoryWhenValidArguments() throws Exception {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("TestCategory");

        when(categoryService.patchCategory(any())).thenReturn(categoryDto);

        mockMvc.perform(patch("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.name", is("TestCategory"), String.class));
    }

    @Test
    void patchCategoryWhenIdIsNullThenReturnBadRequest() throws Exception {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(null);
        categoryDto.setName("TestCategory");

        mockMvc.perform(patch("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchCategoryWhenNameIsEmptyThenReturnBadRequest() throws Exception {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("");

        mockMvc.perform(patch("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCategory() throws Exception {
        doNothing().when(categoryService).deleteCategory(anyLong());

        mockMvc.perform(delete("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void searchEventsWhenArgumentsIsValid() throws Exception {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("TestEvent");

        when(eventService.searchEventByAdmin(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(List.of(eventFullDto));

        mockMvc.perform(get("/admin/events?users=0&states=PUBLISHED&rangeStart=2022-01-06 13:30:38" +
                        "&rangeEnd=2097-09-06 13:30:38&from=0&size=1000&users=15&categories=11&lat=1.0&lon=10.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventFullDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1L), Long.class))
                .andExpect(jsonPath("$[0].title", is("TestEvent"), String.class));

    }

    @Test
    void searchEventsWhenFromIsNegativeThenReturnBadRequest() {

        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/admin/events?users=0&states=PUBLISHED" +
                                        "&rangeStart=2022-01-06 13:30:38&rangeEnd=2097-09-06 13:30:38&from=-10" +
                                        "&size=1000&users=15&categories=11")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("from", "-10")
                                        .param("ids", "1"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: searchEvents.from: must be greater than or " +
                        "equal to 0",
                thrown.getMessage());
    }

    @Test
    void searchEventsWhenSizeIsNegativeThenReturnBadRequest() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/admin/events?users=0&states=PUBLISHED" +
                                        "&rangeStart=2022-01-06 13:30:38&rangeEnd=2097-09-06 13:30:38&from=0" +
                                        "&size=-10&users=15&categories=11")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("from", "-10")
                                        .param("ids", "1"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: searchEvents.size: must be greater than 0",
                thrown.getMessage());
    }

    @Test
    void searchEventsWhenLatIsNegativeThenReturnBadRequest() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/admin/events?users=0&states=PUBLISHED" +
                                        "&rangeStart=2022-01-06 13:30:38&rangeEnd=2097-09-06 13:30:38&from=0" +
                                        "&size=10&users=15&categories=11&lat=-1.0&lon=10.0")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("from", "-10")
                                        .param("ids", "1"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: searchEvents.lat: must be greater than " +
                        "or equal to 0",
                thrown.getMessage());
    }

    @Test
    void searchEventsWhenLonIsNegativeThenReturnBadRequest() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/admin/events?users=0&states=PUBLISHED" +
                                        "&rangeStart=2022-01-06 13:30:38&rangeEnd=2097-09-06 13:30:38&from=0" +
                                        "&size=10&users=15&categories=11&lat=1.0&lon=-10.0")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param("from", "-10")
                                        .param("ids", "1"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                        "javax.validation.ConstraintViolationException: searchEvents.lon: must be greater than " +
                        "or equal to 0",
                thrown.getMessage());
    }

    @Test
    void publishEventThenReturnIsOk() throws Exception {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("Test Event");
        eventFullDto.setPublishedOn(LocalDateTime.now());

        when(eventService.publishEvent(anyLong())).thenReturn(eventFullDto);

        mockMvc.perform(patch("/admin/events/1/publish")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.title", is("Test Event"), String.class));
    }

    @Test
    void rejectEventThenReturnIsOk() throws Exception {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("Test Event");
        eventFullDto.setPublishedOn(LocalDateTime.now());

        when(eventService.rejectEvent(anyLong())).thenReturn(eventFullDto);

        mockMvc.perform(patch("/admin/events/1/reject").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.title", is("Test Event"), String.class));
    }

    @Test
    void editEventByAdminWhenValidArguments() throws Exception {
        AdminUpdateEventRequest adminUpdateEventRequest = new AdminUpdateEventRequest();
        adminUpdateEventRequest.setLocation(new Location());
        adminUpdateEventRequest.setDescription("01234567890123456789");
        adminUpdateEventRequest.setAnnotation("01234567890123456789");
        adminUpdateEventRequest.setTitle("012");

        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setDescription(adminUpdateEventRequest.getDescription());
        eventFullDto.setId(1L);

        when(eventService.putEventByAdmin(anyLong(), any())).thenReturn(eventFullDto);

        mockMvc.perform(put("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminUpdateEventRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.description", is(adminUpdateEventRequest.getDescription()),
                        String.class));
    }

    @Test
    void editEventByAdminWhenDescriptionSizeIs19ThenReturnBadRequest() throws Exception {
        AdminUpdateEventRequest adminUpdateEventRequest = new AdminUpdateEventRequest();
        adminUpdateEventRequest.setLocation(new Location());
        adminUpdateEventRequest.setDescription("0123456789012345678");
        adminUpdateEventRequest.setAnnotation("01234567890123456789");
        adminUpdateEventRequest.setTitle("012");

        mockMvc.perform(put("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminUpdateEventRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void editEventByAdminWhenAnnotationSizeIS19ThenReturnBadRequest() throws Exception {
        AdminUpdateEventRequest adminUpdateEventRequest = new AdminUpdateEventRequest();
        adminUpdateEventRequest.setLocation(new Location());
        adminUpdateEventRequest.setDescription("01234567890123456789");
        adminUpdateEventRequest.setAnnotation("0123456789012345678");
        adminUpdateEventRequest.setTitle("012");

        mockMvc.perform(put("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminUpdateEventRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void editEventByAdminWhenTittleSizeIs2ThenReturnBadRequest() throws Exception {
        AdminUpdateEventRequest adminUpdateEventRequest = new AdminUpdateEventRequest();
        adminUpdateEventRequest.setLocation(new Location());
        adminUpdateEventRequest.setDescription("01234567890123456789");
        adminUpdateEventRequest.setAnnotation("01234567890123456789");
        adminUpdateEventRequest.setTitle("01");

        mockMvc.perform(put("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminUpdateEventRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void editEventByAdminWhenLocationIsNullThenReturnBadRequest() throws Exception {
        AdminUpdateEventRequest adminUpdateEventRequest = new AdminUpdateEventRequest();
        adminUpdateEventRequest.setDescription("01234567890123456789");
        adminUpdateEventRequest.setAnnotation("01234567890123456789");
        adminUpdateEventRequest.setTitle("012");

        mockMvc.perform(put("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminUpdateEventRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewCompilationWhenValidArguments() throws Exception {
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("TestTitle");

        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setTitle(newCompilationDto.getTitle());
        compilationDto.setId(1L);

        when(compilationService.addNewCompilation(any())).thenReturn(compilationDto);

        mockMvc.perform(post("/admin/compilations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.title", is(newCompilationDto.getTitle()), String.class));
    }

    @Test
    void addNewCompilationWhenTitleIsBlankThenReturnBadRequest() throws Exception {
        NewCompilationDto newCompilationDto = new NewCompilationDto();

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addEventToCompilationWhenValidArguments() throws Exception {
        doNothing().when(compilationService).addEventToCompilation(anyLong(), anyLong());

        mockMvc.perform(patch("/admin//compilations/1/events/1"))
                .andExpect(status().isOk());
    }

    @Test
    void pinEventOnMainPage() throws Exception {
        doNothing().when(compilationService).pinCompilationOnMainPage(anyLong());

        mockMvc.perform(patch("/admin/compilations/1/pin"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCompilation() throws Exception {
        doNothing().when(compilationService).deleteCompilation(anyLong());

        mockMvc.perform(delete("/admin//compilations/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteEventFromCompilation() throws Exception {
        doNothing().when(compilationService).deleteEventFromCompilation(anyLong(), anyLong());

        mockMvc.perform(delete("/admin/compilations/1/events/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCompilationFromMainPage() throws Exception {
        doNothing().when(compilationService).deleteCompilationFromMainPage(anyLong());

        mockMvc.perform(delete("/admin//compilations/1"))
                .andExpect(status().isOk());
    }

    @Test
    void addLocationWhenValidArguments() throws Exception {
        LocationDto locationDto = new LocationDto();
        locationDto.setName("TestLocation");
        locationDto.setLat(10.0);
        locationDto.setLon(10.0);

        when(locationService.addLocation(any())).thenReturn(locationDto);

        mockMvc.perform(post("/admin/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(locationDto.getName()), String.class));
    }

    @Test
    void addLocationWhenLatIsNullThenReturnBadRequest() throws Exception {
        LocationDto locationDto = new LocationDto();
        locationDto.setName("TestLocation");
        locationDto.setLon(10.0);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addLocationWhenLonIsNullThenReturnBadRequest() throws Exception {
        LocationDto locationDto = new LocationDto();
        locationDto.setName("TestLocation");
        locationDto.setLat(10.0);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteLocationById() throws Exception {
        doNothing().when(locationService).deleteLocationById(anyLong());

        mockMvc.perform(delete("/admin//locations/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


}