package ru.practicum.ewmservice.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmservice.models.category.dto.CategoryDto;
import ru.practicum.ewmservice.models.compilation.dto.CompilationDto;
import ru.practicum.ewmservice.models.event.dto.EventFullDto;
import ru.practicum.ewmservice.models.event.dto.EventShortDto;
import ru.practicum.ewmservice.models.location.dto.LocationDto;
import ru.practicum.ewmservice.services.CategoryService;
import ru.practicum.ewmservice.services.CompilationService;
import ru.practicum.ewmservice.services.EventService;
import ru.practicum.ewmservice.services.LocationService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicController {
    private final EventService eventService;

    private final CategoryService categoryService;

    private final CompilationService compilationService;

    private final LocationService locationService;


    @GetMapping("/events/{id}")
    public EventFullDto getEventFullInfoById(@PathVariable(name = "id") Long eventId, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();
        log.info("client ip: {}", ip);
        log.info("endpoint path: {}", uri);
        log.info("Получен запрос на получение пдробной информации о событии с id {}", eventId);
        return eventService.getEventFullInfoById(eventId, ip, uri);
    }

    @GetMapping("events")
    public List<EventShortDto> searchEvents(@RequestParam(name = "text", required = false) String text,
                                            @RequestParam(name = "categories", required = false) Long[] categories,
                                            @RequestParam(name = "paid", required = false) Boolean paid,
                                            @RequestParam(name = "rangeStart", required = false) @DateTimeFormat
                                                    (pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                            @RequestParam(name = "rangeEnd", required = false) @DateTimeFormat
                                                    (pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                            @RequestParam(name = "onlyAvailable", defaultValue = "false")
                                            boolean isAvailable,
                                            @RequestParam(name = "sort", required = false) String sort,
                                            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                            Integer from,
                                            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                            HttpServletRequest request,
                                            @PositiveOrZero @RequestParam(name = "lat", required = false) Double lat,
                                            @PositiveOrZero @RequestParam(name = "lon", required = false) Double lon) {
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();
        log.info("client ip: {}", ip);
        log.info("endpoint path: {}", uri);
        log.info("Получен запрос на поиск событий с параметрами: text: {}, categoties: {}, paid: {}, rangeStart: {}," +
                        " rangerEnd: {}, onlyAvailable: {}, sort: {}, from: {}, size: {}", text, categories, paid,
                rangeStart, rangeEnd, isAvailable, sort, from, size);
        return eventService.searchEvents(text, categories, paid, rangeStart, rangeEnd, isAvailable,
                sort, from, size, ip, uri, lat, lon);
    }

    @GetMapping("/compilations")
    public List<CompilationDto> searchCompilations(@RequestParam(name = "pinned", required = false) Boolean pinned,
                                                   @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                                   Integer from,
                                                   @Positive @RequestParam(name = "size", defaultValue = "10")
                                                   Integer size) {
        log.info("Получен запрос на поиск подборок с параметрами: pinned: {}, from: {}, size: {}", pinned, from, size);
        return compilationService.searchCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilationById(@PathVariable(name = "compId") Long compilationId) {
        log.info("Получен запрос на получение подборки с id {}", compilationId);
        return compilationService.getCompilationById(compilationId);
    }

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                           Integer from,
                                           @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получен запрос на получение категорий с параметрами: from: {}, size: {}", from, size);
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getCategoryById(@PathVariable(name = "catId") Long categoryId) {
        log.info("Получен запрос на получение информации про категорию с id {}", categoryId);
        return categoryService.getCategory(categoryId);
    }

    @GetMapping("/locations")
    public List<LocationDto> getAllLocations(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                             Integer from,
                                             @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получен запрос на получение списка локаций с параметрами: from: {}, size: {}", from, size);
        return locationService.getAllLocations(from, size);
    }

    @GetMapping("/locations/events")
    public List<EventShortDto> searchEvenInLocation(@PositiveOrZero @RequestParam(name = "lat") double lat,
                                                    @PositiveOrZero @RequestParam(name = "lon") double lon) {
        log.info("Получен запрос на поиск события по координатам локации: lat: {}, lon: {}", lat, lon);
        return eventService.searchEventInLocation(lat, lon);
    }
}
