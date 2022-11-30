package ru.practicum.ewmservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewmservice.models.participation_request.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    Optional<ParticipationRequest> findByEventAndRequester(Long eventId, Long userId);

    List<ParticipationRequest> findAllByRequesterOrderById(Long userId);

    @Query("select p from  ParticipationRequest as p where p.event = ?1 order by p.id")
    List<ParticipationRequest> findAllRequestsByEvent(Long eventId);
}
