package event.service.events.services;

import interaction.api.dto.event.EventFullDto;
import interaction.api.dto.event.UpdateEventAdminRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminService {

    EventFullDto updateEvent(UpdateEventAdminRequest updateEventAdminRequest, Long eventId);

    List<EventFullDto> getEventsWithAdminFilters(List<Long> users, List<String> states, List<Long> categoryIds,
        LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto getEventById(Long eventId);
}
