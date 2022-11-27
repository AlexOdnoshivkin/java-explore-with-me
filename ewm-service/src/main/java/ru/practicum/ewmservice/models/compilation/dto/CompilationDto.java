package ru.practicum.ewmservice.models.compilation.dto;

import lombok.Data;
import ru.practicum.ewmservice.models.event.dto.EventShortDto;

import java.util.List;

@Data
public class CompilationDto {
    private Long id;
    private Boolean pinned;
    private String title;
    private List<EventShortDto> events;
}
