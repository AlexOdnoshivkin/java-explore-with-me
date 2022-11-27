package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Stat, Long> {
    @Query("select distinct s.ip, s.uri, s.app from Stat as s where s.timesTamp > ?1 " +
            "and s.timesTamp < ?2 " +
            "and s.uri in ?3 ")
    List<String[]> findStatsByUrisUniqueIp(LocalDateTime start, LocalDateTime end, String[] uris);


    @Query("select distinct s.ip, s.uri, s.app from Stat as s where s.timesTamp > ?1 " +
            "and s.timesTamp < ?2")
    List<String[]> findStatsUniqueIp(LocalDateTime start, LocalDateTime end);

    @Query("select s from Stat as s where s.timesTamp > ?1 " +
            "and s.timesTamp < ?2 " +
            "and s.uri in ?3 ")
    List<Stat> findStatsByUris(LocalDateTime start, LocalDateTime end, String[] uris);

    @Query("select s from Stat as s where s.timesTamp > ?1 " +
            "and s.timesTamp < ?2")
    List<Stat> findStats(LocalDateTime start, LocalDateTime end);

}
