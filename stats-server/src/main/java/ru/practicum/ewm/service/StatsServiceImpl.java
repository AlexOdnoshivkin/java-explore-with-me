package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    public void saveStatsHit(EndpointHit endpointHit) {
        Stat stat = Mapper.toStatFromEndpointHit(endpointHit);
        stat.setTimesTamp(LocalDateTime.now());
        statsRepository.save(stat);
        log.debug("Статистика сохранена в базе данных: {}", stat);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique) {
        Map<String, ViewStats> uriStats = new HashMap<>();// Мапа уникальных uri
        if (uris != null) {
            for (String s : uris) {
                uriStats.put(s, new ViewStats(s, null, 0L));
            }
        }
        List<Stat> queryResult;
        if (unique) {
            if (uris == null) {
                queryResult = new ArrayList<>();
                // Получаем список из массивов с уникальными параметрами ip, uri, app
                List<String[]> uniqueIpStats = statsRepository.findStatsUniqueIp(start, end);
                // Далее формируем список Stat из полученных массивов
                for (String[] stat : uniqueIpStats) {
                    Stat uniqueIpStat = Stat.builder()
                            .ip(stat[0])
                            .uri(stat[1])
                            .app(stat[2])
                            .build();
                    queryResult.add(uniqueIpStat);
                }
            } else {
                queryResult = new ArrayList<>();
                List<String[]> uniqueIpStats = statsRepository.findStatsByUrisUniqueIp(start, end, uris);
                for (String[] stat : uniqueIpStats) {
                    Stat uniqueIpStat = Stat.builder()
                            .ip(stat[0])
                            .uri(stat[1])
                            .app(stat[2])
                            .build();
                    queryResult.add(uniqueIpStat);
                }
            }

        } else {
            if (uris == null) {
                queryResult = statsRepository.findStats(start, end).stream().peek((Stat stat) ->
                                uriStats.put(stat.getUri(), new ViewStats()))
                        .collect(Collectors.toList());
            } else {
                queryResult = statsRepository.findStatsByUris(start, end, uris);
            }
        }
        for (Stat stat : queryResult) { // Формируем мапу уникальных uri с кол-вом обращений
            ViewStats viewStats = uriStats.get(stat.getUri());
            if (viewStats == null) {
                uriStats.put(stat.getUri(), new ViewStats(stat.getApp(), stat.getUri(), 1L));
                viewStats = uriStats.get(stat.getUri());
            }
            viewStats.setHits(viewStats.getHits() + 1L);
            viewStats.setApp(stat.getApp());
            viewStats.setUri(stat.getUri());
            uriStats.put(stat.getUri(), viewStats);
        }
        List<ViewStats> result = new ArrayList<>(uriStats.values());
        log.debug("Получена статистика из базы данных: {}", queryResult);
        return result;
    }
}
