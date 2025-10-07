package main.service.events.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import main.service.events.dto.EventFullDto;
import main.service.events.dto.EventShortDto;
import main.service.events.dto.NewEventDto;
import main.service.events.dto.UpdateEventUserRequest;
import main.service.events.services.PrivateService;
import main.service.request.dto.EventRequestStatusUpdateRequestDto;
import main.service.request.dto.EventRequestStatusUpdateResultDto;
import main.service.request.dto.ParticipationRequestDto;
import main.service.request.service.RequestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateController {
    PrivateService privateService;
    RequestService requestService;

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable
                                             @Positive
                                             Long userId,

                                             @RequestParam(defaultValue = "0")
                                             @PositiveOrZero
                                             Integer from,

                                             @RequestParam(defaultValue = "10")
                                             @Positive
                                             Integer size) {
        log.info("Получение событий, добавленных текущим пользователем");
        return privateService.getUserEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@Valid @RequestBody NewEventDto newEventDto,
                                    @PathVariable @Positive Long userId) {
        log.info("Добавление нового события пользователем");
        return privateService.createEvent(newEventDto, userId);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByEventIdAndUserId(@PathVariable @Positive Long userId,
                                                   @PathVariable @Positive Long eventId) {
        log.info("Получение полной информации о событии добавленном текущим пользователем");
        return privateService.getEventByEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByEventId(@PathVariable @Positive Long userId,
                                             @PathVariable @Positive Long eventId,
                                             @Valid @RequestBody UpdateEventUserRequest updateEventDto) {
        log.info("Изменение события, добавленного текущим пользователем");
        return privateService.updateEventByEventId(updateEventDto, userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequestsByOwner(@PathVariable @Positive Long userId,
                                                                 @PathVariable @Positive Long eventId) {
        log.info("Получение информации о запросах на участие в событии текущего пользователя");
        return requestService.getCurrentUserEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResultDto updateEventRequest(@PathVariable @Positive Long userId,
                                                                @PathVariable @Positive Long eventId,

                                                                @RequestBody
                                                                @Valid
                                                                EventRequestStatusUpdateRequestDto update) {
        log.info("Изменение статуса заявок на участие в событии текущего пользователя");
        return requestService.updateParticipationRequestsStatus(userId, eventId, update);
    }
}