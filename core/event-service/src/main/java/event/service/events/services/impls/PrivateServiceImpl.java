package event.service.events.services.impls;

import feign.FeignException;
import interaction.api.dto.event.EventFullDto;
import interaction.api.dto.event.EventShortDto;
import interaction.api.dto.event.NewEventDto;
import interaction.api.dto.event.UpdateEventUserRequest;
import interaction.api.dto.user.UserShortDto;
import interaction.api.exception.BadRequestException;
import interaction.api.exception.ConflictException;
import interaction.api.exception.NotFoundException;
import interaction.api.exception.UserOperationFailedException;
import event.service.category.service.CategoryService;
import event.service.feign.client.UserClient;
import stats.client.StatsClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import event.service.category.model.Category;
import interaction.api.enums.EventState;
import interaction.api.enums.StateAction;
import event.service.events.mapper.EventMapper;
import event.service.events.model.EventModel;
import event.service.events.repository.EventRepository;
import event.service.events.services.PrivateService;
import event.service.location.Location;
import event.service.location.LocationMapper;
import event.service.location.service.LocationServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stats.dto.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class PrivateServiceImpl implements PrivateService {
    EventRepository eventRepository;
    EventMapper eventMapper;
    UserClient userClient;
    CategoryService categoryService;
    LocationServiceImpl locationService;
    LocationMapper locationMapper;
    StatsClient statsClient;

    @Override
    public EventFullDto createEvent(NewEventDto newEvent, Long userId) {
        log.debug("Получен запрос на создание нового события");

        UserShortDto user = findExistingUser(userId);

        Category category = categoryExistence(newEvent.getCategory());

        Location location = locationService.save(locationMapper.toEntity(newEvent.getLocationDto()));

        EventModel event = eventMapper.toEntity(newEvent, category, userId, location);

        return eventMapper.toFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto updateEventByEventId(UpdateEventUserRequest update, Long userId, Long eventId) {
        log.debug("Получен запрос на обновление события пользователем");
        findExistingUser(userId);
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие  c id= %d не найдено", eventId)));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Невозможно обновить опубликованное событие");
        }

        if (update.getEventDate() != null && update.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Изменяемая дата не может быть в прошлом");
        }

        changeEventState(event, update);
        updateEventFields(event, update);

        log.debug("Сборка события для ответа");

        EventFullDto result = eventMapper.toFullDto(event);
        result.setViews(getAmountOfViews(List.of(event)).get(eventId));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.debug("Получен запрос для получения событий пользователя");
        findExistingUser(userId);

        Page<EventModel> events = eventRepository.findByInitiatorId(
                userId,
                PageRequest.of(from / size, size, Sort.by("eventDate").descending())
        );

        Map<Long, Long> views = getAmountOfViews(events.getContent());
        return events.getContent().stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    dto.setViews(views.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByEventId(Long userId, Long eventId) {
        log.debug("Получен запрос события по id");
        findExistingUser(userId);
        EventModel event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id= %d " +
                                                                       "у пользователя с id= %d не найдено", eventId, userId)));

        log.debug("Сборка события для ответа");
        EventFullDto result = eventMapper.toFullDto(event);
        result.setViews(getAmountOfViews(List.of(event)).get(eventId));
        return result;
    }

    @Override
    public List<EventModel> findAllById(List<Long> ids) {
        return eventRepository.findAllById(ids);
    }

    @Override
    public Optional<EventModel> findById(Long id) {
        return eventRepository.findById(id);
    }

    private UserShortDto findExistingUser(Long userId) {
        if (userId == null) {
            throw new BadRequestException("id пользователя не может быть null");
        }

        try {
            return userClient.getUserById(userId);
        } catch (FeignException.FeignClientException e) {
            log.warn("Клиентская ошибка (4xx) при получении пользователя id {}: {}", userId, e.getMessage());
            throw new UserOperationFailedException(String.format("Ошибка при получении пользователя по id %d: %s", userId, e.getMessage()));
        } catch (FeignException e) {
            log.error("Серверная ошибка (5xx) при получении пользователя id {}: {}", userId, e.getMessage(), e);
            throw new UserOperationFailedException(String.format("user-service недоступен для пользователя с id %d", userId));
        }
    }

    private Category categoryExistence(Long categoryId) {
        return categoryService.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Категория c id= %d не найдена", categoryId)));
    }

    private void changeEventState(EventModel event, UpdateEventUserRequest update) {
        if (update.getState() != null) {
            if (update.getState() == StateAction.SEND_TO_REVIEW) event.setState(EventState.PENDING);
            if (update.getState() == StateAction.CANCEL_REVIEW) event.setState(EventState.CANCELED);
        }
    }

    private void updateEventFields(EventModel event, UpdateEventUserRequest update) {
        if (update.getAnnotation() != null) {
            event.setAnnotation(update.getAnnotation());
        }

        if (update.getCategory() != null) {
            Category category = categoryExistence(update.getCategory());
            event.setCategory(category);
        }

        if (update.getDescription() != null) {
            event.setDescription(update.getDescription());
        }

        if (update.getEventDate() != null) {
            if (update.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Событие не может быть раньше чем через 2 часа");
            }
            event.setEventDate(update.getEventDate());
        }

        if (update.getPaid() != null) {
            event.setPaid(update.getPaid());
        }

        if (update.getParticipantLimit() != null) {
            event.setParticipantLimit(update.getParticipantLimit());
        }

        if (update.getRequestModeration() != null) {
            event.setRequestModeration(update.getRequestModeration());
        }

        if (update.getTitle() != null) {
            event.setTitle(update.getTitle());
        }

        if (update.getLocation() != null) {
            event.setLocation(locationMapper.toEntity(update.getLocation()));
        }
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
}
