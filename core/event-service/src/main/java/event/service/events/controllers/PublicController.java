package event.service.events.controllers;

import event.service.events.services.PublicService;
import interaction.api.dto.event.EventFullDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static interaction.api.utility.AppConstants.DATE_TIME_FORMAT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class PublicController {
    PublicService publicService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEvents(@RequestParam(required = false) String text,
                                        @RequestParam(required = false) List<Long> categories,
                                        @RequestParam(required = false) Boolean paid,

                                        @RequestParam(required = false)
                                        @DateTimeFormat(pattern = DATE_TIME_FORMAT)
                                        LocalDateTime rangeStart,

                                        @RequestParam(required = false)
                                        @DateTimeFormat(pattern = DATE_TIME_FORMAT)
                                        LocalDateTime rangeEnd,

                                        @RequestParam(defaultValue = "false") Boolean onlyAvailable,

                                        @RequestParam(required = false)
                                        @Pattern(regexp = "EVENT_DATE|VIEWS")
                                        String sort,

                                        @RequestParam(defaultValue = "0") Integer from,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        HttpServletRequest request) {
        log.info("Поступил запрос на получение событий от ноунейма");
        return publicService.getEventsWithFilters(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size, request);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventById(@PathVariable @Positive Long eventId,
                                     HttpServletRequest request,
                                     @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.info("Поступил запрос на получение события по id: {} от пользователя", userId);
        return publicService.getEventById(eventId, request, userId);
    }

    @GetMapping("/recommendations")
    public List<EventFullDto> getRecommendations(@RequestParam Long max,
                                                  @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.info("Поступил запрос на получение рекомендаций");
        return publicService.getRecommendation(userId, max);
    }

    @PutMapping("/{eventId}/like")
    public void likeEvent(@PathVariable Long eventId, @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.info("Получен запрос на лайк для события");
        publicService.addLike(eventId, userId);
    }
}
