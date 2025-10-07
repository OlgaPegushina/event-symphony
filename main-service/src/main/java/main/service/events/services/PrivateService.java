package main.service.events.services;

import main.service.events.dto.EventFullDto;
import main.service.events.dto.EventShortDto;
import main.service.events.dto.NewEventDto;
import main.service.events.dto.UpdateEventUserRequest;

import java.util.List;

public interface PrivateService {

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto createEvent(NewEventDto newEventDto, Long userId);

    EventFullDto getEventByEventId(Long userId, Long eventId);

    EventFullDto updateEventByEventId(UpdateEventUserRequest updateEventDto, Long userId, Long eventId);
}
