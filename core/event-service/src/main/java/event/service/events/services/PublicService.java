package event.service.events.services;

import interaction.api.dto.event.EventFullDto;
import interaction.api.dto.event.EventShortDto;
import jakarta.servlet.http.HttpServletRequest;
import event.service.events.model.EventModel;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicService {
    EventFullDto getEventById(Long eventId, HttpServletRequest request);

    List<EventModel> findAllByCategoryId(Long catId);


    List<EventShortDto> getEventsWithFilters(String text, List<Long> categoryIds, Boolean paid, LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                             Integer size, HttpServletRequest request);
}
