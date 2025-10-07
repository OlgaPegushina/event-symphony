package main.service.events.services.impls;

import stats.client.StatsClient;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import main.service.events.dto.EventFullDto;
import main.service.events.dto.EventShortDto;
import main.service.events.enums.EventState;
import main.service.events.mapper.EventMapper;
import main.service.events.model.EventModel;
import main.service.events.repository.EventRepository;
import main.service.events.services.PublicService;
import main.service.exception.BadRequestException;
import main.service.exception.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stats.dto.dto.EndpointHitDto;
import stats.dto.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class PublicServiceImpl implements PublicService {
    EventRepository eventRepository;
    EventMapper eventMapper;
    JPAQueryFactory jpaQueryFactory;
    StatsClient statsClient;

    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsWithFilters(String text, List<Long> categories, Boolean paid,
                                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                                    Integer size, HttpServletRequest request) {

        log.debug("Получен запрос на получение public событий с фильтрами");
        if ((rangeStart != null) && (rangeEnd != null) && (rangeStart.isAfter(rangeEnd)))
            throw new BadRequestException("Время начала на может быть позже окончания");

        List<EventModel> events = eventRepository.findAllByFiltersPublic(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, PageRequest.of(from, size));

        try {
            statsClient.postHit(EndpointHitDto.builder()
                    .app("main-service")
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Не удалось отправить запрос о сохранении на сервер статистики");
        }

        Map<Long, Long> views = getAmountOfViews(events);
        log.debug("Собираем события для ответа");

        return events.stream()
                .map(eventModel -> {
                    EventShortDto eventShort = eventMapper.toShortDto(eventModel);
                    eventShort.setViews(views.getOrDefault(eventModel.getId(), 0L));
                    return eventShort;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {

        log.debug("Получен запрос на получение события по id");
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id= %d не было найдено", eventId)));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Событие с id= %d недоступно, так как не опубликовано", eventId));
        }

        try {
            statsClient.postHit(EndpointHitDto.builder()
                    .app("main-service")
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Не удалось отправить запрос о сохранении на сервер статистики");
        }

        log.debug("Собираем событие для ответа");
        EventFullDto result = eventMapper.toFullDto(event);
        Map<Long, Long> views = getAmountOfViews(List.of(event));
        result.setViews(views.getOrDefault(event.getId(), 0L));

        return result;
    }

    private Map<Long, Long> getAmountOfViews(List<EventModel> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .distinct()
                .collect(Collectors.toList());

        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusMinutes(5);

        Map<Long, Long> viewsMap = new HashMap<>();
        try {
            log.debug("Получение статистики по времени для URI: {} с {} по {}", uris, startTime, endTime);
            List<ViewStatsDto> stats = statsClient.getStatistics(
                    startTime,
                    endTime,
                    uris,
                    true
            );
            log.debug("Получение статистики");
            if (stats != null && !stats.isEmpty()) {
                for (ViewStatsDto stat : stats) {
                    Long eventId = Long.parseLong(stat.getUri().substring("/events/".length()));
                    viewsMap.put(eventId, stat.getHits());
                }
            }
        } catch (Exception e) {
            log.error("Не удалось получить статистику");
        }
        return viewsMap;
    }

    @Override
    public List<EventModel> findAllByCategoryId(Long catId) {
        return eventRepository.findAllByCategoryId(catId);
    }
}
