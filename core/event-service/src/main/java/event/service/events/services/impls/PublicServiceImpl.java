package event.service.events.services.impls;

import com.querydsl.jpa.impl.JPAQueryFactory;
import event.service.events.mapper.EventMapper;
import event.service.events.model.EventModel;
import event.service.events.repository.EventRepository;
import event.service.events.services.PublicService;
import event.service.feign.client.RequestClient;
import feign.FeignException;
import interaction.api.dto.event.EventFullDto;
import interaction.api.dto.request.ParticipationRequestDto;
import interaction.api.enums.EventState;
import interaction.api.exception.BadRequestException;
import interaction.api.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stats.client.AnalyzerClient;
import stats.client.CollectorClient;
import ru.practicum.grpc.ewm.dashboard.message.RecommendedEventProto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    AnalyzerClient analyzerClient;
    CollectorClient collectorClient;
    RequestClient requestClient;

    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsWithFilters(String text, List<Long> categoryIds, Boolean paid,
                                                   LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                                   Integer size, HttpServletRequest request) {

        log.debug("Вызван метод getEventsWithFilters. Параметры: text='{}', categoryIds={}, paid={}, rangeStart={}, " +
                  "rangeEnd={}, onlyAvailable={}, sort='{}', from={}, size={}",
                text, categoryIds, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        if ((rangeStart != null) && (rangeEnd != null) && (rangeStart.isAfter(rangeEnd)))
            throw new BadRequestException("Время начала на может быть позже окончания");

        List<EventModel> events = eventRepository.findAllByFiltersPublic(
                text, categoryIds, paid, rangeStart, rangeEnd, onlyAvailable, PageRequest.of(from, size));

        fillConfirmedRequestsInModels(events);

        log.debug("Собираем события для ответа");

        return events.stream().
                map(eventModel -> {
                    EventFullDto dto = eventMapper.toFullDto(eventModel);
                    dto.setRating(analyzerClient.getInteractionsCount(List.of(eventModel.getId()))
                            .map(RecommendedEventProto::getScore)
                            .findFirst()
                            .orElse(0.0));
                    return dto;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long eventId, HttpServletRequest request, Long userId) {

        log.debug("Получен запрос на получение события по id");
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id= %d не было найдено", eventId)));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Событие с id= %d недоступно, так как не опубликовано", eventId));
        }

        log.debug("Собираем событие для ответа");
        EventFullDto result = eventMapper.toFullDto(event);
        result.setRating(analyzerClient.getInteractionsCount(List.of(event.getId()))
                .map(RecommendedEventProto::getScore)
                .findFirst()
                .orElse(0.0));

        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventModel> findAllByCategoryId(Long catId) {
        return eventRepository.findAllByCategoryId(catId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getRecommendation(Long userId, Long max) {
        log.info("Получен запрос на рекомендации для пользователя");
        List<Long> eventIds = analyzerClient.getRecommendationsForUser(userId, max)
                .map(RecommendedEventProto::getEventId)
                .toList();
        List<EventModel> events = eventRepository.findAllByIdIn(eventIds);

        fillConfirmedRequestsInModels(events);

        return events.stream()
                .map(eventModel -> {
                    EventFullDto dto = eventMapper.toFullDto(eventModel);
                    dto.setRating(analyzerClient.getInteractionsCount(List.of(eventModel.getId()))
                            .map(RecommendedEventProto::getScore)
                            .findFirst()
                            .orElse(0.0));
                    return dto;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public void addLike(Long eventId, Long userId) {
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (requestClient.checkRegistration(eventId, userId)) {
            collectorClient.collectUserAction(userId, eventId, "ACTION_LIKE", Instant.now());
        } else {
            throw new NotFoundException("Пользователь не регистрировался на данное событие");
        }
    }

    private void fillConfirmedRequestsInModels(List<EventModel> events) {
        if (events == null || events.isEmpty()) return;

        List<Long> eventIds = events.stream()
                .map(EventModel::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (eventIds.isEmpty()) {
            events.forEach(e -> e.setConfirmedRequests(0L));
            return;
        }

        try {
            Map<Long, List<ParticipationRequestDto>> confirmedMap = requestClient.prepareConfirmedRequests(eventIds);
            events.forEach(event -> {
                List<ParticipationRequestDto> reqs = confirmedMap.getOrDefault(event.getId(), Collections.emptyList());
                event.setConfirmedRequests((long) reqs.size());
            });
        } catch (FeignException e) {
            log.warn("Не удалось заполнить confirmedRequests для eventIds {}: Fallback 0L", eventIds, e);
            events.forEach(event -> event.setConfirmedRequests(0L));
        }
    }
}
