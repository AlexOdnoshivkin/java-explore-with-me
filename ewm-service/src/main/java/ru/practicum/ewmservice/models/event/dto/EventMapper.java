package ru.practicum.ewmservice.models.event.dto;

import org.mapstruct.*;
import ru.practicum.ewmservice.models.category.dto.CategoryMapper;
import ru.practicum.ewmservice.models.event.Event;
import ru.practicum.ewmservice.models.location.Location;
import ru.practicum.ewmservice.models.user.dto.UserMapper;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({@Mapping(source = "annotation", target = "annotation"),
            @Mapping(source = "category", target = "category.id"),
            @Mapping(source = "description", target = "description"),
            @Mapping(source = "eventDate", target = "eventDate"),
            @Mapping(source = "eventId", target = "id"),
            @Mapping(source = "paid", target = "paid"),
            @Mapping(source = "participantLimit", target = "participantLimit"),
            @Mapping(source = "title", target = "title"),
    })
    void updateEvent(UpdateEventRequest updateEventRequest, @MappingTarget Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({@Mapping(source = "annotation", target = "annotation"),
            @Mapping(source = "category", target = "category.id"),
            @Mapping(source = "description", target = "description"),
            @Mapping(source = "eventDate", target = "eventDate"),
            @Mapping(source = "location.lon", target = "locationLon"),
            @Mapping(source = "location.lat", target = "locationLat"),
            @Mapping(source = "paid", target = "paid"),
            @Mapping(source = "participantLimit", target = "participantLimit"),
            @Mapping(source = "requestModeration", target = "requestModeration"),
            @Mapping(source = "title", target = "title"),
    })
    void updateEventFromAdmin(AdminUpdateEventRequest adminUpdateEventRequest, @MappingTarget Event event);

    @Mappings({@Mapping(source = "annotation", target = "annotation"),
            @Mapping(source = "category", target = "category.id"),
            @Mapping(source = "description", target = "description"),
            @Mapping(source = "eventDate", target = "eventDate"),
            @Mapping(source = "location.lon", target = "locationLon"),
            @Mapping(source = "location.lat", target = "locationLat"),
            @Mapping(source = "paid", target = "paid"),
            @Mapping(source = "participantLimit", target = "participantLimit"),
            @Mapping(source = "requestModeration", target = "requestModeration"),
            @Mapping(source = "title", target = "title"),
    })
    Event toEventFromNewEventDto(NewEventDto newEventDto);

    default EventShortDto toEventShortDtoFromEvent(Event event) {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setCategory(CategoryMapper.toCategoryDtoFromCategory(event.getCategory()));
        eventShortDto.setConfirmedRequests(event.getConfirmedRequests());
        eventShortDto.setEventDate(event.getEventDate());
        eventShortDto.setId(event.getId());
        eventShortDto.setInitiator(UserMapper.mapToUserShortDtoFromUser(event.getInitiator()));
        eventShortDto.setPaid(event.isPaid());
        eventShortDto.setTitle(event.getTitle());
        return eventShortDto;
    }

    default EventFullDto toEventFullDtoFromEvent(Event event) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setCategory(CategoryMapper.toCategoryDtoFromCategory(event.getCategory()));
        eventFullDto.setConfirmedRequests(event.getConfirmedRequests());
        eventFullDto.setCreatedOn(event.getCreatedOn());
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setEventDate(event.getEventDate());
        eventFullDto.setId(event.getId());
        eventFullDto.setInitiator(UserMapper.mapToUserShortDtoFromUser(event.getInitiator()));
        eventFullDto.setLocation(new Location(event.getLocationLat(), event.getLocationLon()));
        eventFullDto.setPaid(event.isPaid());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setPublishedOn(event.getPublishedOn());
        eventFullDto.setRequestModeration(event.isRequestModeration());
        eventFullDto.setState(event.getState());
        eventFullDto.setTitle(event.getTitle());
        return eventFullDto;
    }

}


