package main.service.events.services;

import jakarta.servlet.http.HttpServletRequest;
import main.service.events.dto.EventFullDto;
import main.service.events.dto.EventShortDto;
import main.service.events.model.EventModel;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicService {
    EventFullDto getEventById(Long eventId, HttpServletRequest request);

    List<EventModel> findAllByCategoryId(Long catId);


    List<EventShortDto> getEventsWithFilters(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                             Integer size, HttpServletRequest request);
}
