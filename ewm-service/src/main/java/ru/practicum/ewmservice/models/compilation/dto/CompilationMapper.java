package ru.practicum.ewmservice.models.compilation.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.models.compilation.Compilation;
import ru.practicum.ewmservice.models.event.dto.EventMapper;
import ru.practicum.ewmservice.models.event.dto.EventShortDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventMapper eventMapper;

    public Compilation toCompilationFromNewCompilationDto(NewCompilationDto newCompilationDto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setPinned(newCompilationDto.isPinned());
        return compilation;
    }

    public CompilationDto toCompilationDtoFromCompilation(Compilation compilation) {
        List<EventShortDto> events = compilation.getEvents().stream()
                .map(eventMapper::toEventShortDtoFromEvent)
                .collect(Collectors.toList());
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(compilation.getId());
        compilationDto.setPinned(compilation.getPinned());
        compilationDto.setTitle(compilation.getTitle());
        compilationDto.setEvents(events);
        return compilationDto;
    }
}
