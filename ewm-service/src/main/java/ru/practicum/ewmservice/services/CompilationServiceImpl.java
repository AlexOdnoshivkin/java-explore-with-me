package ru.practicum.ewmservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.FromSizeRequest;
import ru.practicum.ewmservice.exceptions.EntityNotFoundException;
import ru.practicum.ewmservice.models.compilation.Compilation;
import ru.practicum.ewmservice.models.compilation.dto.CompilationDto;
import ru.practicum.ewmservice.models.compilation.dto.CompilationMapper;
import ru.practicum.ewmservice.models.compilation.dto.NewCompilationDto;
import ru.practicum.ewmservice.models.event.Event;
import ru.practicum.ewmservice.repositories.CompilationRepository;
import ru.practicum.ewmservice.repositories.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;

    private final EventRepository eventRepository;

    private final CompilationMapper mapper;

    @Override
    @Transactional
    public CompilationDto addNewCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = mapper.toCompilationFromNewCompilationDto(newCompilationDto);
        if (newCompilationDto.getEvents() == null) {
            compilation.setEvents(new ArrayList<>());
        } else {
            List<Event> events = eventRepository.findAllById(newCompilationDto.getEvents());
            compilation.setEvents(events);
        }
        CompilationDto result = mapper.toCompilationDtoFromCompilation(compilationRepository.save(compilation));
        log.debug("Подборка сохранена в базе данных {}", result);
        return result;
    }

    @Override
    public CompilationDto getCompilationById(Long compilationId) {
        Compilation compilation = checkCompilationInDatabase(compilationId);
        CompilationDto result = mapper.toCompilationDtoFromCompilation(compilation);
        log.debug("Получена поборка из базы данных: {}", result);
        return result;
    }

    @Override
    @Transactional
    public void addEventToCompilation(Long compilationId, Long eventId) {
        Compilation compilation = checkCompilationInDatabase(compilationId);
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new EntityNotFoundException("Событие с id " + eventId + " не найдено в базе данных");
        }

        List<Event> events = compilation.getEvents();
        events.add(eventOptional.get());
        compilation.setEvents(events);
        compilationRepository.save(compilation);
        log.debug("Событие с id {}, добавлено в подборку с id {}", eventId, compilationId);
    }

    @Override
    @Transactional
    public void pinCompilationOnMainPage(Long compilationId) {
        Compilation compilation = checkCompilationInDatabase(compilationId);
        compilation.setPinned(true);
        compilationRepository.save(compilation);
        log.debug("Подборка с id {}, закреплена на главной странице", compilationId);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compilationId) {
        checkCompilationInDatabase(compilationId);
        compilationRepository.deleteById(compilationId);
        log.debug("Подборка с id {} удалена из базы данных", compilationId);
    }

    @Override
    @Transactional
    public void deleteEventFromCompilation(Long compilationId, Long eventId) {
        Compilation compilation = checkCompilationInDatabase(compilationId);
        List<Event> events = compilation.getEvents();
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new EntityNotFoundException("Событие с id " + eventId + " не найдено в базе данных");
        }
        events.remove(eventOptional.get());
        compilation.setEvents(events);
        compilationRepository.save(compilation);
        log.debug("Событие с id {} удалено из подборки с id {}", eventId, compilationId);
    }

    @Override
    @Transactional
    public void deleteCompilationFromMainPage(Long compilationId) {
        Compilation compilation = checkCompilationInDatabase(compilationId);
        compilation.setPinned(false);
        compilationRepository.save(compilation);
        log.debug("Подборка с id {}, удалена с главной страницы", compilationId);
    }

    @Override
    public List<CompilationDto> searchCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = FromSizeRequest.of(from, size);
        List<CompilationDto> result;
        if (pinned == null) {
            result = compilationRepository.findAll().stream()
                    .map(mapper::toCompilationDtoFromCompilation)
                    .collect(Collectors.toList());
        } else {
            result = compilationRepository.searchCompilation(pinned, pageable).stream()
                    .map(mapper::toCompilationDtoFromCompilation)
                    .collect(Collectors.toList());
        }
        log.debug("Получены подборки из базы данных: {}", result);
        return result;
    }

    private Compilation checkCompilationInDatabase(Long compilationId) {
        Optional<Compilation> compilationOptional = compilationRepository.findById(compilationId);
        if (compilationOptional.isEmpty()) {
            throw new EntityNotFoundException("Подборка с id " + compilationId + " не найдена в базе данных");
        }
        return compilationOptional.get();
    }
}
