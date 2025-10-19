package event.service.events.services;

import event.service.events.model.EventModel;
import interaction.api.dto.event.EventFullDto;
import interaction.api.dto.event.EventShortDto;
import interaction.api.dto.event.NewEventDto;
import interaction.api.dto.event.UpdateEventUserRequest;

import java.util.List;
import java.util.Optional;

public interface PrivateService {

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto createEvent(NewEventDto newEventDto, Long userId);

    EventFullDto getEventByEventId(Long userId, Long eventId);

    EventFullDto updateEventByEventId(UpdateEventUserRequest updateEventDto, Long userId, Long eventId);

    List<EventModel> findAllById(List<Long> ids);

    Optional<EventModel> findById(Long id);
}
