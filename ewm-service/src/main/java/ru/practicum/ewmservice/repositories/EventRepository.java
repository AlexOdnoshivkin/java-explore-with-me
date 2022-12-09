package ru.practicum.ewmservice.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewmservice.models.event.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    @Query("select e from Event as e where e.initiator.id = ?1")
    List<Event> getUserEvents(Long userId, Pageable pageable);

    @Query("select e from Event as e where e.category.id = ?1 order by e.id")
    List<Event> getEventsByCategory(Long categoryId);


    @Query(value = "SELECT * " +
            "FROM events as e " +
            "INNER JOIN locations as l on e.location_id = l.id " +
            "where distance(l.lat, l.lon, ?1, ?2) <= l.radius",
    nativeQuery = true)
    List<Event> getEventsByLocation(double lat, double lon);
}
