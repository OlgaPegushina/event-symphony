package event.service.events.services.impls;

import event.service.category.service.CategoryService;
import event.service.feign.client.RequestClient;
import feign.FeignException;
import interaction.api.dto.event.EventFullDto;
import interaction.api.dto.event.UpdateEventAdminRequest;
import interaction.api.dto.request.ParticipationRequestDto;
import interaction.api.exception.BadRequestException;
import interaction.api.exception.ConflictException;
import interaction.api.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;
import stats.client.StatsClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import event.service.category.model.Category;
import interaction.api.enums.EventState;
import interaction.api.enums.StateActionAdmin;
import event.service.events.mapper.EventMapper;
import event.service.events.model.EventModel;
import event.service.events.repository.EventRepository;
import event.service.events.services.AdminService;
import event.service.location.Location;
import event.service.location.LocationMapper;
import event.service.location.service.LocationServiceImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stats.dto.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@SuppressWarnings("unused")
public class AdminServiceImpl implements AdminService {
    EventMapper eventMapper;
    EventRepository eventRepository;
    CategoryService categoryService;
    LocationServiceImpl locationService;
    LocationMapper locationMapper;
    StatsClient statsClient;
    RequestClient requestClient;

    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsWithAdminFilters(List<Long> userIds, List<String> states, List<Long> categoryIds,
                                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {

        log.debug("Получен запрос на получения админ события по фильтрам с параметрами: " +
                  "userIds={}, states={}, categoryIds={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                userIds, states, categoryIds, rangeStart, rangeEnd, from, size);

        if ((rangeStart != null) && (rangeEnd != null) && (rangeStart.isAfter(rangeEnd)))
            throw new BadRequestException("Время начала не может быть позже времени конца");

        List<EventState> eventStates = null;
        if (!CollectionUtils.isEmpty(states)) {
            try {
                eventStates = states.stream()
                        .map(String::toUpperCase)
                        .map(EventState::valueOf)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Передан неизвестный статус события: " + e.getMessage());
            }
        }

        List<Long> finalUserIds = CollectionUtils.isEmpty(userIds) ? null : userIds;
        List<EventState> finalEventStates = CollectionUtils.isEmpty(eventStates) ? null : eventStates;
        List<Long> finalCategoryIds = CollectionUtils.isEmpty(categoryIds) ? null : categoryIds;

        Pageable pageable = PageRequest.of(from / size, size);

        Page<EventModel> eventsPage = eventRepository.findAllByFiltersAdmin(
                finalUserIds,
                finalEventStates,
                finalCategoryIds,
                rangeStart,
                rangeEnd,
                pageable
        );

        List<EventModel> events = eventsPage.getContent();
        fillConfirmedRequestsInModels(events);
        Map<Long, Long> views = getAmountOfViews(events);

        log.debug("Собираем событие для ответа");
        return events.stream()
                .map(eventModel -> {
                    EventFullDto eventFull = eventMapper.toFullDto(eventModel);
                    eventFull.setViews(views.getOrDefault(eventFull.getId(), 0L));
                    return eventFull;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    public EventFullDto updateEvent(UpdateEventAdminRequest updateRequest, Long eventId) {
        log.debug("Получен запрос на обновление события");
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id= %d не найдено", eventId)));

        if (updateRequest.getEventDate() != null && updateRequest.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Изменяемая дата не может быть в прошлом");
        }

        validateEventState(event, updateRequest.getState());
        changeEventState(event, updateRequest.getState());
        updateEventFields(event, updateRequest);

        fillConfirmedRequestInModel(event);
        eventRepository.save(event);

        log.debug("Сборка события для ответа");
        EventFullDto result = eventMapper.toFullDto(event);
        result.setViews(getAmountOfViews(List.of(event)).getOrDefault(eventId, 0L));

        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventById(Long eventId) {
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id= %d не найдено", eventId)));

        fillConfirmedRequestInModel(event);

        EventFullDto dto = eventMapper.toFullDto(event);
        dto.setViews(getAmountOfViews(List.of(event)).getOrDefault(eventId, 0L));
        return dto;
    }

    private void fillConfirmedRequestsInModels(List<EventModel> events) {
        if (CollectionUtils.isEmpty(events)) return;

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

    private void fillConfirmedRequestInModel(EventModel event) {
        if (event != null && event.getId() != null) {
            fillConfirmedRequestsInModels(List.of(event));
        } else if (event != null) {
            event.setConfirmedRequests(0L);
        }
    }

    private void validateEventState(EventModel event, StateActionAdmin state) {
        if (state == null) return;

        if (state == StateActionAdmin.PUBLISH_EVENT && event.getState() != EventState.PENDING) {
            throw new ConflictException("Только события в статусе ожидание могут быть опубликованы");
        }
        if (state == StateActionAdmin.REJECT_EVENT && event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Только неопубликованные события могут быть отменены");
        }
    }

    private void changeEventState(EventModel event, StateActionAdmin state) {
        if (state == null) return;

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Дата события не может быть в прошлом");
        }

        if (state == StateActionAdmin.PUBLISH_EVENT) {
            if ((event.getEventDate().isBefore(LocalDateTime.now().plusHours(1)))) {
                throw new ConflictException("Время старта события должно быть позже");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }

        if (state == StateActionAdmin.REJECT_EVENT) {
            event.setState(EventState.CANCELED);
        }
    }

    private void updateEventFields(EventModel event, UpdateEventAdminRequest updateRequest) {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.getCategory() != null) {
            Category category = categoryService.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(category);
        }

        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }

        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getLocationDto() != null) {
            Location newLocation = locationMapper.toEntity(updateRequest.getLocationDto());
            locationService.save(newLocation);
            event.setLocation(newLocation);
        }
    }

    private Map<Long, Long> getAmountOfViews(List<EventModel> events) {
        if (CollectionUtils.isEmpty(events)) {
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
            if (!CollectionUtils.isEmpty(stats)) {
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
 }
