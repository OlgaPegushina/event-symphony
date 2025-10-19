package request.service.controller;

import interaction.api.dto.request.EventRequestStatusUpdateRequestDto;
import interaction.api.dto.request.EventRequestStatusUpdateResultDto;
import interaction.api.dto.request.ParticipationRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import request.service.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
@Validated
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class PrivateRequestController {
    RequestService requestService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequests(@PathVariable("userId") @Positive Long requesterId) {
        log.info("Получаем запросы");
        return requestService.getRequests(requesterId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable("userId") @Positive Long requesterId,
                                                 @RequestParam("eventId") @Positive Long eventId) {
        log.info("Создаем запрос id={}", requesterId);
        return requestService.createRequest(requesterId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(@PathVariable("userId") @Positive Long requesterId,
                                                 @PathVariable("requestId") @Positive Long requestId) {
        log.info("Отменяем запрос");
        return requestService.cancelRequest(requesterId, requestId);
    }

    @GetMapping("/{eventId}")
    public List<ParticipationRequestDto> getCurrentUserEventRequests(@PathVariable("userId") @Positive Long initiatorId,
                                                                     @PathVariable("eventId") @Positive Long eventId) {
        log.info("Получение информации о запросах на участие в событии текущего пользователя");
        return requestService.getCurrentUserEventRequests(initiatorId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResultDto updateParticipationRequestsStatus(
            @PathVariable("userId") @Positive Long initiatorId,
            @PathVariable("eventId") @Positive Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequestDto e) {
        log.info("Изменение статуса заявок на участие в событии текущего пользователя");
        return requestService.updateParticipationRequestsStatus(initiatorId, eventId, e);
    }
}
