package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.EndpointHit;
import ru.practicum.ewm.model.ViewStats;
import ru.practicum.ewm.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService service;

    @PostMapping("/hit")
    public void saveStatsHit(@RequestBody EndpointHit endpointHit) {
        log.info("Получен запрос на сохранение статистики: {}", endpointHit);
        service.saveStatsHit(endpointHit);
    }

    @GetMapping("stats")
    List<ViewStats> getStats(@RequestParam(name = "start") @DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
                             LocalDateTime start,
                             @RequestParam(name = "end") @DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
                             LocalDateTime end,
                             @RequestParam(name = "uris", required = false) String[] uris,
                             @RequestParam(name = "unique", defaultValue = "false") boolean unique
                             ) {
        log.info("Получен запрос на получение статистики с параметрами: start: {}, end: {}, uris: {}, unique: {}",
                start, end, uris, unique);
        return service.getStats(start, end, uris, unique);
    }

}
