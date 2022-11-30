package ru.practicum.ewmservice.models.participation_request.dto;

import org.mapstruct.Mapper;
import ru.practicum.ewmservice.models.participation_request.ParticipationRequest;

@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {
    ParticipationRequestDto toParticipationRequestDtoFromParticipationRequest(ParticipationRequest request);
}
