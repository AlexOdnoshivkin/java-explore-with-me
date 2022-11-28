package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.Stat;
import ru.practicum.ewm.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Stat, Long> {
    @Query("select new ru.practicum.ewm.model.ViewStats(s.app, s.uri, count (distinct s.ip)) from Stat as s " +
            "where s.timesTamp > ?1 " +
            "and s.timesTamp < ?2 " +
            "and s.uri in ?3 " +
            "group by s.app, s.uri")
    List<ViewStats> findStatsByUrisUniqueIp(LocalDateTime start, LocalDateTime end, String[] uris);


    @Query("select new ru.practicum.ewm.model.ViewStats(s.app, s.uri, count (distinct s.ip)) from Stat as s " +
            "where s.timesTamp > ?1 " +
            "and s.timesTamp < ?2 " +
            "group by s.app, s.uri")
    List<ViewStats> findStatsUniqueIp(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.ewm.model.ViewStats(s.app, s.uri, count (s.ip)) from Stat as s " +
            "where s.timesTamp > ?1 " +
            "and s.timesTamp < ?2 " +
            "and s.uri in ?3 " +
            "group by s.app, s.uri")
    List<ViewStats> findStatsByUris(LocalDateTime start, LocalDateTime end, String[] uris);

    @Query("select new ru.practicum.ewm.model.ViewStats(s.app, s.uri, count (s.ip)) from Stat as s " +
            "where s.timesTamp > ?1 " +
            "and s.timesTamp < ?2 " +
            "group by s.app, s.uri")
    List<ViewStats> findStats(LocalDateTime start, LocalDateTime end);

}
