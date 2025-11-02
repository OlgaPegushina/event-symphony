package request.service.controller;

import interaction.api.dto.request.EventRequestStatusUpdateResultDto;
import interaction.api.dto.request.ParticipationRequestDto;
import interaction.api.dto.request.RequestStatusUpdateDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import request.service.service.RequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class PrivateRequestController {
    RequestService requestService;

    @GetMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequests(@PathVariable("userId") @Positive Long requesterId) {
        log.info("Получаем запросы");
        return requestService.getRequests(requesterId);
    }

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable("userId") @Positive Long requesterId,
                                                 @RequestParam("eventId") Long eventId) {
        log.info("Создаем запрос id={}", requesterId);
        return requestService.createRequest(requesterId, eventId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(@PathVariable("userId") @Positive Long requesterId,
                                                 @PathVariable("requestId") @Positive Long requestId) {
        log.info("Отменяем запрос");
        return requestService.cancelRequest(requesterId, requestId);
    }

    @GetMapping("/users/{userId}/requests/{eventId}")
    public List<ParticipationRequestDto> getCurrentUserEventRequests(@PathVariable("userId") @Positive Long initiatorId,
                                                                     @PathVariable("eventId") @Positive Long eventId) {
        log.info("Получение информации о запросах на участие в событии текущего пользователя");
        return requestService.getCurrentUserEventRequests(initiatorId, eventId);
    }

    @PatchMapping(value = "/users/{userId}/requests/{eventId}/status",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)  // Match контракту Feign
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResultDto updateParticipationRequestsStatus(
            @PathVariable("userId") @Positive Long initiatorId,
            @PathVariable("eventId") @Positive Long eventId,
            @Valid @RequestBody RequestStatusUpdateDto updateDto) {  // <-- Добавьте @Valid для валидации DTO (event, updateRequest)
        log.info("PATCH received: userId={}, eventId={}, dto={}", initiatorId, eventId, updateDto);  // Debug-лог для подтверждения
        return requestService.updateParticipationRequestsStatus(initiatorId, eventId, updateDto);
    }

    @GetMapping("/users/requests/confirmed")
    public Map<Long, List<ParticipationRequestDto>> prepareConfirmedRequests(@RequestParam List<Long> eventIds) {
        log.info("Получен список eventIds {} для получения подтверждённых заявок", eventIds);
        return requestService.prepareConfirmedRequests(eventIds);
    }

    @GetMapping("/users/requests/confirmed/{eventId}/{userId}")
    Boolean checkRegistration(@PathVariable Long eventId, @PathVariable Long userId) {
        log.info("Проверка регистрации пользователя");
        return requestService.checkRegistration(eventId, userId);
    }
}
