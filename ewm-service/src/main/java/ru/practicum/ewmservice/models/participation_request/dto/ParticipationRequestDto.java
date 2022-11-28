package ru.practicum.ewmservice.models.participation_request.dto;

import lombok.Data;
import ru.practicum.ewmservice.models.participation_request.Status;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ParticipationRequestDto {
    private Long id;
    @NotNull
    private LocalDateTime created;
    @NotNull
    private Long event;
    @NotNull
    private Long requester;
    @NotNull
    private Status status;
}
