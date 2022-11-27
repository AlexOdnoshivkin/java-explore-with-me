package ru.practicum.ewmservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewmservice.models.event.Event;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewmservice.models.event.State;


import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("select e from Event as e where e.initiator.id = ?1")
    List<Event> getUserEvents(Long userId, Pageable pageable);

    @Query("select e from Event as e where e.category.id = ?1 order by e.id")
    List<Event> getEventsByCategory(Long categoryId);

    @Query("select e from Event as e where (lower(e.annotation) like lower(concat('%', ?1, '%')) or " +
            " lower(e.description) like lower(concat('%', ?1, '%'))) " +
            " and e.category.id in (?2)" +
            " and e.paid = ?3" +
            " and e.eventDate > ?4 and  e.eventDate < ?5 " +
            " and e.participantLimit > e.confirmedRequests ")
    List<Event> searchEventsWithPaid(String text, Long[] ids, Boolean isPaid, LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                     boolean isAvailable, Pageable pageable);

    @Query("select e from Event as e where (lower(e.annotation) like lower(concat('%', ?1, '%')) or " +
            " lower(e.description) like lower(concat('%', ?1, '%'))) " +
            " and e.category.id in (?2)" +
            " and e.eventDate > ?3 and  e.eventDate < ?4 " +
            " and e.participantLimit > e.confirmedRequests ")
    List<Event> searchEventsWithoutPaid(String text, Long[] ids, LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        boolean isAvailable, Pageable pageable);

    @Query("select e from Event as e where e.initiator.id in ?1 " +
            " and e.state in ?2" +
            " and e.category.id in ?3 " +
            " and e.eventDate > ?4 and e.eventDate < ?5 " +
            " order by e.id")
    List<Event> searchEventsWithUsersAndCategoryAndStates(Long[] users, State[] states, Long[] categories,
                                                          LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                          Pageable pageable);

}
