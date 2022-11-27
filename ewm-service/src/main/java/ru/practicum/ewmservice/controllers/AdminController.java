package ru.practicum.ewmservice.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.models.category.dto.CategoryDto;
import ru.practicum.ewmservice.models.category.dto.NewCategoryDto;
import ru.practicum.ewmservice.models.compilation.dto.CompilationDto;
import ru.practicum.ewmservice.models.compilation.dto.NewCompilationDto;
import ru.practicum.ewmservice.models.event.State;
import ru.practicum.ewmservice.models.event.dto.AdminUpdateEventRequest;
import ru.practicum.ewmservice.models.event.dto.EventFullDto;
import ru.practicum.ewmservice.models.user.dto.NewUserRequest;
import ru.practicum.ewmservice.models.user.dto.UserDto;
import ru.practicum.ewmservice.services.CategoryService;
import ru.practicum.ewmservice.services.CompilationService;
import ru.practicum.ewmservice.services.EventService;
import ru.practicum.ewmservice.services.UserService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "/admin")
public class AdminController {

    private final UserService userService;

    private final CategoryService categoryService;

    private final CompilationService compilationService;

    private final EventService eventService;

    @PostMapping("/users")
    public UserDto addNewUser(@RequestBody @Validated NewUserRequest newUser) {
        log.info("Запрос на добавление пользователя {}", newUser);
        return userService.addNewUser(newUser);
    }


    @GetMapping("/users")
    public List<UserDto> getUsers(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                  @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                  @RequestParam(name = "ids") Long[] ids) {
        log.info("Получен запрос на получение пользователей с параметрами: from {}, size{}, ids {}", from, size, ids);
        return userService.getUsers(from, size, ids);
    }


    @DeleteMapping("/users/{userId}")
    public void deleteUser(@PathVariable(name = "userId") Long userId) {
        log.info("Получен запрос на удаление пользователя с id {}", userId);
        userService.deleteUser(userId);
    }

    @PostMapping("/categories")
    public CategoryDto addNewCategory(@RequestBody @Validated NewCategoryDto newCategoryDto) {
        log.info("Получен запрос на добавление категории {}", newCategoryDto);
        return categoryService.addNewCategory(newCategoryDto);
    }

    @PatchMapping("/categories")
    public CategoryDto patchCategory(@RequestBody @Validated CategoryDto categoryDto) {
        log.info("Получен запрос на изменение категории {}", categoryDto);
        return categoryService.patchCategory(categoryDto);
    }

    @DeleteMapping("/categories/{catId}")
    public void deleteCategory(@PathVariable(name = "catId") Long categoryId) {
        log.info("Получен запрос на удаление категории с id {}", categoryId);
        categoryService.deleteCategory(categoryId);
    }

    @GetMapping("/events")
    public List<EventFullDto> searchEvents(@RequestParam(name = "users", required = false) Long[] users,
                                           @RequestParam(name = "states", required = false) State[] states,
                                           @RequestParam(name = "categories", required = false) Long[] categories,
                                           @RequestParam(name = "rangeStart", required = false) @DateTimeFormat
                                                   (pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                           @RequestParam(name = "rangeEnd", required = false) @DateTimeFormat
                                                   (pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                           @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                           int from,
                                           @Positive @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        log.info("Получен запрос на поиск события с параметрами: users: {}, states: {}, categories: {}, " +
                        "rangeStart: {}, rangeEnd: {}, from: {}, size: {}", users, states, categories, rangeStart,
                rangeEnd, from, size);
        return eventService.searchEventByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("events/{eventId}/publish")
    public EventFullDto publishEvent(@PathVariable(name = "eventId") Long eventId) {
        log.info("Получен запрос на публицацию события с id {}", eventId);
        return eventService.publishEvent(eventId);
    }

    @PatchMapping("events/{eventId}/reject")
    public EventFullDto rejectEvent(@PathVariable(name = "eventId") Long eventId) {
        log.info("Получен запрос на отклонение события с id {}", eventId);
        return eventService.rejectEvent(eventId);
    }

    @PutMapping("events/{eventId}")
    public EventFullDto editingEventByAdmin(@PathVariable(name = "eventId") Long eventId,
                                            @RequestBody AdminUpdateEventRequest eventToUpdate) {
        log.info("Получен запрос на редактирование события с id {} администратором: {}", eventId, eventToUpdate);
        return eventService.putEventByAdmin(eventId, eventToUpdate);
    }

    @PostMapping("/compilations")
    public CompilationDto addNewCompilation(@RequestBody @Validated NewCompilationDto newCompilationDto) {
        log.info("Получен запрос на добавление новой подборки {}", newCompilationDto);
        return compilationService.addNewCompilation(newCompilationDto);
    }

    @PatchMapping("/compilations/{compId}/events/{eventId}")
    public void addEventToCompilation(@PathVariable(name = "compId") Long compilationId,
                                      @PathVariable(name = "eventId") Long eventId) {
        log.info("Получен запрос на добавление события с id {} в подборку с id {}", eventId, compilationId);
        compilationService.addEventToCompilation(compilationId, eventId);
    }

    @PatchMapping("/compilations/{compId}/pin")
    public void pinEventOnMainPage(@PathVariable(name = "compId") Long compilationId) {
        log.info("Получен запрос на добавление подборки с id {} на главную страницу", compilationId);
        compilationService.pinCompilationOnMainPage(compilationId);
    }

    @DeleteMapping("/compilations/{compId}")
    public void deleteCompilation(@PathVariable(name = "compId") Long compilationId) {
        log.info("Получен запрос на удаление подборки с id {}", compilationId);
        compilationService.deleteCompilation(compilationId);
    }

    @DeleteMapping("/compilations/{compId}/events/{eventId}")
    public void deleteEventFromCompilation(@PathVariable(name = "compId") Long compilationId,
                                           @PathVariable(name = "eventId") Long eventId) {
        log.info("Получен запрос на удаление события с id {} из подборки с id {}", eventId, compilationId);
        compilationService.deleteEventFromCompilation(compilationId, eventId);
    }

    @DeleteMapping("/compilations/{compId}/pin")
    public void deleteCompilationFromMainPage(@PathVariable(name = "compId") Long compilationId) {
        log.info("Получен запрос на удаление подборки с id {} c главной страницы", compilationId);
        compilationService.deleteCompilationFromMainPage(compilationId);
    }

}
