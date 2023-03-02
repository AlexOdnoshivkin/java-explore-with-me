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
import ru.practicum.ewmservice.models.compilation.dto.CompilationDto;
import ru.practicum.ewmservice.models.compilation.dto.NewCompilationDto;
import ru.practicum.ewmservice.models.event.dto.EventFullDto;
import ru.practicum.ewmservice.models.event.dto.EventMapper;
import ru.practicum.ewmservice.models.event.dto.NewEventDto;
import ru.practicum.ewmservice.models.location.dto.LocationDto;
import ru.practicum.ewmservice.models.user.dto.NewUserRequest;
import ru.practicum.ewmservice.models.user.dto.UserDto;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CompilationServiceImplTest {
    private final EntityManager em;

    private final CompilationService compilationService;

    private final EventService eventService;

    private final UserService userService;

    private final CategoryService categoryService;

    private final EventMapper eventMapper;

    private EventFullDto event;


    @BeforeEach
    void setUp() {
        NewUserRequest newUser = new NewUserRequest();
        newUser.setName("Boris");
        newUser.setEmail("BorisBritva@gmail.com");
        UserDto user = userService.addNewUser(newUser);

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
        newEventDto.setParticipantLimit(1);

        event = eventService.addNewEvent(newEventDto, user.getId());
    }

    @AfterEach
    void afterEach() {
        em.createNativeQuery("truncate table compilations");
    }

    @Test
    void addNewCompilationWhenValidData() {
        // Проверка успешного сценария
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setEvents(List.of(event.getId()));
        newCompilationDto.setTitle("TestTitle");
        newCompilationDto.setPinned(false);

        CompilationDto savedCompilation = compilationService.addNewCompilation(newCompilationDto);

        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setTitle("TestTitle");
        compilationDto.setId(savedCompilation.getId());
        compilationDto.setPinned(false);
        compilationDto.setEvents(List.of(eventMapper.toEventShortDtoFromEventFullDto(event)));

        assertEquals(compilationDto, savedCompilation);
    }

    @Test
    void getCompilationByIdWhenValidData() {
        // Проверка успешного сценария
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setEvents(List.of(1L));
        newCompilationDto.setTitle("TestTitle");
        newCompilationDto.setPinned(false);

        CompilationDto savedCompilation = compilationService.addNewCompilation(newCompilationDto);

        CompilationDto compilationDto = compilationService.getCompilationById(savedCompilation.getId());

        assertEquals(savedCompilation, compilationDto);
    }

    @Test
    void getCompilationWhenCompilationNotFoundThenThrowException() {
        // Проверка случая, когда подюорка не найдена в базе данных
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        compilationService.getCompilationById(1L));

        assertEquals("Подборка с id 1 не найдена в базе данных", thrown.getMessage());
    }

    @Test
    void addEventToCompilationWhenValidData() {
        // Проверка успешного сценария
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("TestTitle");
        newCompilationDto.setPinned(false);
        CompilationDto compilationDto = compilationService.addNewCompilation(newCompilationDto);

        assertEquals(0, compilationDto.getEvents().size());

        compilationService.addEventToCompilation(compilationDto.getId(), event.getId());

        CompilationDto savedCompilation = compilationService.getCompilationById(compilationDto.getId());

        assertEquals(1, savedCompilation.getEvents().size());

        assertEquals(eventMapper.toEventShortDtoFromEventFullDto(event), savedCompilation.getEvents().get(0));
    }

    @Test
    void addEventToCompilationWhenCompilationNotFoundThenThrowException() {
        // Проверка случая, когда подборка не найдена в базе данных
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        compilationService.addEventToCompilation(1L, event.getId()));

        assertEquals("Подборка с id 1 не найдена в базе данных", thrown.getMessage());
    }

    @Test
    void addEventToCompilationWhenEventNotFoundThenThrowException() {
        // Проверка случвя, когда событие не найдено в базе данных
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("TestTitle");
        newCompilationDto.setPinned(false);
        CompilationDto compilationDto = compilationService.addNewCompilation(newCompilationDto);

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        compilationService.addEventToCompilation(compilationDto.getId(),0L));

        assertEquals("Событие с id 0 не найдено в базе данных", thrown.getMessage());
    }

    @Test
    void pinCompilationOnMainPageWhenValidData() {
        // Проверка успешного сценария
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("TestTitle");
        newCompilationDto.setPinned(false);
        CompilationDto compilationDto = compilationService.addNewCompilation(newCompilationDto);

        compilationService.pinCompilationOnMainPage(compilationDto.getId());

        CompilationDto savedCompilation = compilationService.getCompilationById(compilationDto.getId());

        assertEquals(true, savedCompilation.getPinned());
    }

    @Test
    void pinCompilationOnMainPageWhenCompilationNotFoundThenThrowException() {
        // Проверка случая, когда подюорка не найдена в базе данных
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        compilationService.pinCompilationOnMainPage(0L));

        assertEquals("Подборка с id 0 не найдена в базе данных", thrown.getMessage());
    }

    @Test
    void deleteCompilationWhenValidData() {
        // Проверка успешного сценария
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("TestTitle");
        newCompilationDto.setPinned(false);
        CompilationDto compilationDto = compilationService.addNewCompilation(newCompilationDto);

        compilationService.deleteCompilation(compilationDto.getId());

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        compilationService.getCompilationById(compilationDto.getId()));

        assertEquals("Подборка с id " + compilationDto.getId() + " не найдена в базе данных",
                thrown.getMessage());
    }

    @Test
    void deleteCompilationWhenCompilationNotFoundThenThrowException() {
        // Проверка случая, когда подборка не найдена
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        compilationService.deleteCompilation(0L));

        assertEquals("Подборка с id 0 не найдена в базе данных", thrown.getMessage());
    }

    @Test
    void deleteEventFromCompilationWhenValidData() {
        // Проверка успешного сценария
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("TestTitle");
        newCompilationDto.setPinned(false);
        newCompilationDto.setEvents(List.of(event.getId()));
        CompilationDto compilationDto = compilationService.addNewCompilation(newCompilationDto);

        assertEquals(1, compilationDto.getEvents().size());

        compilationService.deleteEventFromCompilation(compilationDto.getId(), event.getId());

        CompilationDto savedCompilation = compilationService.getCompilationById(compilationDto.getId());

        assertEquals(0, savedCompilation.getEvents().size());
    }

    @Test
    void deleteEventFromCompilationWhenCompilationNotFoundThenThrowException() {
        // Проверка случая, когда подборка не найдена в базе данных
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        compilationService.deleteEventFromCompilation(0L, event.getId()));

        assertEquals("Подборка с id 0 не найдена в базе данных", thrown.getMessage());
    }

    @Test
    void deleteEventFromCompilationWhenEventNotFoundThenThrowException() {
        // Проверка случая, когда событие не найдено в базе данных
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("TestTitle");
        newCompilationDto.setPinned(false);
        newCompilationDto.setEvents(List.of(event.getId()));
        CompilationDto compilationDto = compilationService.addNewCompilation(newCompilationDto);

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        compilationService.deleteEventFromCompilation(compilationDto.getId(),0L));

        assertEquals("Событие с id 0 не найдено в базе данных", thrown.getMessage());
    }

    @Test
    void searchCompilationWhenPinnedIsFalse() {
        // Проверка случая, когда в поле pinned значение false
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("TestTitle");
        newCompilationDto.setPinned(false);
        newCompilationDto.setEvents(List.of(event.getId()));
        CompilationDto compilationDto = compilationService.addNewCompilation(newCompilationDto);

        NewCompilationDto anotherNewCompilation = new NewCompilationDto();
        anotherNewCompilation.setTitle("AnotherTitle");
        anotherNewCompilation.setPinned(true);
        anotherNewCompilation.setEvents(List.of(event.getId()));
        compilationService.addNewCompilation(anotherNewCompilation);

        List<CompilationDto> result = compilationService.searchCompilations(false, 0, 10);

        assertEquals(List.of(compilationDto), result);
    }

    @Test
    void searchCompilationWhenPinnedIsTrue() {
        // Проверка случая, когда в поле pinned значение true
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("TestTitle");
        newCompilationDto.setPinned(false);
        newCompilationDto.setEvents(List.of(event.getId()));
        compilationService.addNewCompilation(newCompilationDto);

        NewCompilationDto anotherNewCompilation = new NewCompilationDto();
        anotherNewCompilation.setTitle("AnotherTitle");
        anotherNewCompilation.setPinned(true);
        anotherNewCompilation.setEvents(List.of(event.getId()));
        CompilationDto compilationDto = compilationService.addNewCompilation(anotherNewCompilation);

        List<CompilationDto> result = compilationService.searchCompilations(true, 0, 10);

        assertEquals(List.of(compilationDto), result);
    }




}