package ru.practicum.ewmservice.services;

import ru.practicum.ewmservice.models.participation_request.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    ParticipationRequestDto addNewParticipationRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getUserParticipationRequests(Long userId);

    List<ParticipationRequestDto> getUserEventParticipationRequests(Long userId, Long eventId);

    ParticipationRequestDto cancelParticipationRequestByUser(Long userId, Long requestId);

    ParticipationRequestDto confirmParticipationRequest(Long userId, Long eventId, Long requestId);

    ParticipationRequestDto rejectParticipationRequest(Long userId, Long eventId, Long requestId);
}
