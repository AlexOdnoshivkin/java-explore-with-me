package ru.practicum.ewmservice.models.participation_request.dto;

import lombok.Data;
import ru.practicum.ewmservice.models.participation_request.Status;

import java.time.LocalDateTime;

@Data
public class ParticipationRequestDto {
    private Long id;
    private LocalDateTime created;
    private Long event;
    private Long requester;
    private Status status;
}
