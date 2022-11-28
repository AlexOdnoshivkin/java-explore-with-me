package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.model.EndpointHit;
import ru.practicum.ewm.model.Mapper;
import ru.practicum.ewm.model.Stat;
import ru.practicum.ewm.model.ViewStats;
import ru.practicum.ewm.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

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

        List<ViewStats> result;
        if (unique) {
            if (uris == null) {
                result = statsRepository.findStatsUniqueIp(start, end);
            } else {
                result = statsRepository.findStatsByUrisUniqueIp(start, end, uris);
            }
        } else {
            if (uris == null) {
                result = statsRepository.findStats(start, end);
            } else {
                result = statsRepository.findStatsByUris(start, end, uris);
            }
        }
        log.debug("Получена статистика из базы данных: {}", result);
        return result;
    }
}
