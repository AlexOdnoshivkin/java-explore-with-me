package ru.practicum.ewmservice.models.compilation.dto;

import lombok.Data;
import ru.practicum.ewmservice.models.event.dto.EventShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CompilationDto {
    private Long id;
    @NotNull
    private Boolean pinned;
    @NotBlank
    private String title;
    private List<EventShortDto> events;
}
