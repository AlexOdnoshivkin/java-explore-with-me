package ru.practicum.ewmservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.exceptions.EntityNotFoundException;
import ru.practicum.ewmservice.models.event.Event;
import ru.practicum.ewmservice.models.participation_request.ParticipationRequest;
import ru.practicum.ewmservice.models.participation_request.Status;
import ru.practicum.ewmservice.models.participation_request.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.models.participation_request.dto.ParticipationRequestMapper;
import ru.practicum.ewmservice.repositories.EventRepository;
import ru.practicum.ewmservice.repositories.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final EventServiceImpl eventService;

    private final UserServiceImpl userService;

    private final ParticipationRequestRepository participationRequestRepository;

    private final EventRepository eventRepository;

    private final ParticipationRequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto addNewParticipationRequest(Long userId, Long eventId) {
        userService.checkUserInDatabase(userId);
        Event event = eventService.checkEventInDatabase(eventId);
        if (event.getInitiator().getId().equals(userId)) {
            throw new IllegalStateException("Ининциатор не может ооставить заявку на участие в своём событии");
        }
        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new IllegalStateException("Превыен лимит заявок на участие в событии");
        }
        if (participationRequestRepository.findByEventAndRequester(eventId, userId).isPresent()) {
            throw new IllegalStateException("Запрос уже существует");
        }

        ParticipationRequest participationRequest = new ParticipationRequest();
        participationRequest.setRequester(userId);
        participationRequest.setEvent(eventId);
        participationRequest.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        if (!event.isRequestModeration()) {
            participationRequest.setStatus(Status.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else {
            participationRequest.setStatus(Status.PENDING);
        }
        eventRepository.save(event);
        ParticipationRequestDto result = requestMapper.toParticipationRequestDtoFromParticipationRequest(
                participationRequestRepository.save(participationRequest));
        log.debug("Сохранена заявка на участие {} в базе данных", result);
        return result;
    }

    @Override
    public List<ParticipationRequestDto> getUserParticipationRequests(Long userId) {
        userService.checkUserInDatabase(userId);
        List<ParticipationRequestDto> result = participationRequestRepository.findAllByRequesterOrderById(userId)
                .stream()
                .map(requestMapper::toParticipationRequestDtoFromParticipationRequest)
                .collect(Collectors.toList());
        log.debug("Получены запросы на участие пользователя с id {} : {}", userId, result);
        return result;
    }

    @Override
    public List<ParticipationRequestDto> getUserEventParticipationRequests(Long userId, Long eventId) {
        userService.checkUserInDatabase(userId);
        eventService.checkEventInDatabase(eventId);
        eventService.cancelEventFromUser(userId, eventId);
        List<ParticipationRequestDto> result = participationRequestRepository.findAllRequestsByEvent(eventId)
                .stream()
                .map(requestMapper::toParticipationRequestDtoFromParticipationRequest)
                .collect(Collectors.toList());
        log.debug("Получены запросы на участие в событии с id: {}, соданного пользователем с id: {} : {}",
                eventId, userId, result);
        return result;
    }

    @Override
    public ParticipationRequestDto cancelParticipationRequestByUser(Long userId, Long requestId) {
        userService.checkUserInDatabase(userId);
        Optional<ParticipationRequest> requestOptional = participationRequestRepository.findById(requestId);
        if (requestOptional.isEmpty()) {
            throw new EntityNotFoundException("Запрос на участие не найден");
        }
        ParticipationRequest request = requestOptional.get();
        if (!request.getRequester().equals(userId)) {
            throw new IllegalStateException("Пользователь не является автором запроса");
        }
        request.setStatus(Status.CANCELED);
        ParticipationRequestDto result = requestMapper.toParticipationRequestDtoFromParticipationRequest(
                participationRequestRepository.save(request));
        log.debug("Запрос на участие отменён пользователем {}", result);
        return result;
    }

    @Override
    @Transactional
    public ParticipationRequestDto confirmParticipationRequest(Long userId, Long eventId, Long requestId) {
        userService.checkUserInDatabase(userId);
        eventService.checkEventInDatabase(eventId);
        ParticipationRequest request = checkParticipationRequestInDatabase(requestId);
        request.setStatus(Status.CONFIRMED);
        ParticipationRequestDto result = requestMapper
                .toParticipationRequestDtoFromParticipationRequest(participationRequestRepository.save(request));
        log.debug("Подтверждена заявка {}", result);
        return result;
    }

    @Override
    @Transactional
    public ParticipationRequestDto rejectParticipationRequest(Long userId, Long eventId, Long requestId) {
        userService.checkUserInDatabase(userId);
        eventService.checkEventInDatabase(eventId);
        ParticipationRequest request = checkParticipationRequestInDatabase(requestId);
        request.setStatus(Status.REJECTED);
        ParticipationRequestDto result = requestMapper
                .toParticipationRequestDtoFromParticipationRequest(participationRequestRepository.save(request));
        log.debug("Отклонена заявка {}", result);
        return result;
    }

    private ParticipationRequest checkParticipationRequestInDatabase(Long requestId) {
        Optional<ParticipationRequest> participationRequestOptional = participationRequestRepository.findById(requestId);
        if (participationRequestOptional.isEmpty()) {
            throw new EntityNotFoundException("Заявка на участие с id " + requestId + " не найдена в базе данных");
        }
        return participationRequestOptional.get();
    }
}
