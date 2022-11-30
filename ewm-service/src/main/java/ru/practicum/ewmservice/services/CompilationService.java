package ru.practicum.ewmservice.services;

import ru.practicum.ewmservice.models.compilation.dto.CompilationDto;
import ru.practicum.ewmservice.models.compilation.dto.NewCompilationDto;

import java.util.List;

public interface CompilationService {
    CompilationDto addNewCompilation(NewCompilationDto newCompilationDto);

    CompilationDto getCompilationById(Long compilationId);

    void addEventToCompilation(Long compilationId, Long eventId);

    void pinCompilationOnMainPage(Long compilationId);

    void deleteCompilation(Long compilationId);

    void deleteEventFromCompilation(Long compilationId, Long eventId);

    void deleteCompilationFromMainPage(Long compilationId);

    List<CompilationDto> searchCompilations(Boolean pinned, int from, int size);
}
