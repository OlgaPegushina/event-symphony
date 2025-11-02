package request.service.service;

import feign.FeignException;
import interaction.api.dto.event.EventFullDto;
import interaction.api.dto.request.EventRequestStatusUpdateRequestDto;
import interaction.api.dto.request.EventRequestStatusUpdateResultDto;
import interaction.api.dto.request.ParticipationRequestDto;
import interaction.api.dto.request.RequestStatusUpdateDto;
import interaction.api.enums.EventState;
import interaction.api.enums.RequestStatus;
import interaction.api.exception.BadRequestException;
import interaction.api.exception.ConflictException;
import interaction.api.exception.DuplicatedDataException;
import interaction.api.exception.EventOperationFailedException;
import interaction.api.exception.NotFoundException;
import interaction.api.exception.UserOperationFailedException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import request.service.feign.client.EventClient;
import request.service.feign.client.UserClient;
import request.service.mapper.RequestMapper;
import request.service.model.ParticipationRequest;
import request.service.repository.RequestRepository;
import stats.client.CollectorClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@SuppressWarnings("unused")
@Slf4j
public class RequestServiceImpl implements RequestService {
    RequestRepository requestRepository;
    RequestMapper requestMapper;
    EventClient eventClient;
    UserClient userClient;
    CollectorClient collectorClient;

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getRequests(Long requesterId) {
        validateUserExist(requesterId);

        return requestRepository.findAllByRequesterId(requesterId)
                .stream()
                .sorted(Comparator.comparing(ParticipationRequest::getCreated))
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(Long requesterId, Long eventId) {
        collectorClient.collectUserAction(requesterId, eventId, "ACTION_REGISTER", Instant.now());
        return requestMapper.toParticipationRequestDto(requestRepository.save(validateRequest(requesterId, eventId)));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long requesterId, Long requestId) {
        validateUserExist(requesterId);
        ParticipationRequest participationRequest = validateRequestExist(requesterId, requestId);

        participationRequest.setStatus(RequestStatus.CANCELED);
        return requestMapper.toParticipationRequestDto(participationRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getCurrentUserEventRequests(Long initiatorId, Long eventId) {
        validateUserExist(initiatorId);
        return requestRepository.findByEventId(eventId).stream()
                .sorted(Comparator.comparing(ParticipationRequest::getCreated))
                .map(requestMapper::toParticipationRequestDto).toList();
    }

    @Override
    public EventRequestStatusUpdateResultDto updateParticipationRequestsStatus(Long initiatorId, Long eventId,
                                                                               RequestStatusUpdateDto updateDto) {
        log.info("Начало обновления статусов запроса на участие для инициатора id: {}, события id: {}",
                initiatorId, eventId);
        validateUserExist(initiatorId);
        EventRequestStatusUpdateRequestDto updateRequest = updateDto.getUpdateRequest();
        EventFullDto event = updateDto.getEvent();

        if (!event.getInitiator().equals(initiatorId)) {
            log.error("Попытка изменить статус не инициатором события. Инициатор: {}, Запрос: {}",
                    event.getInitiator(), initiatorId);
            throw new ConflictException("Только инициатор события может менять статус запроса на участие в событии");
        }

        long limit = event.getParticipantLimit();
        log.info("Лимит: {}", limit);

        EventRequestStatusUpdateResultDto result = new EventRequestStatusUpdateResultDto();

        if (!event.getRequestModeration() || limit == 0) {
            log.info("Запросы на участие не требуют модерации или лимит участников равен 0.");
            return result;
        }

        List<Long> requestIds = updateRequest.getRequestIds();
        RequestStatus status = updateRequest.getStatus();

        if (!status.equals(RequestStatus.REJECTED) && !status.equals(RequestStatus.CONFIRMED)) {
            log.error("Недопустимый статус запроса: {}", status);
            throw new BadRequestException("Статус должен быть REJECTED или CONFIRMED");
        }

        if (requestRepository.countByIdInAndEventId(requestIds, eventId) != requestIds.size()) {
            log.error("Некоторые запросы не соответствуют событию с id: {}", eventId);
            throw new ConflictException(String.format("Не все запросы соответствуют событию с id= %d", eventId));
        }

        if (requestRepository
                    .countByEventIdAndStatusEquals(eventId, RequestStatus.CONFIRMED) >= limit) {
            log.error("Достигнут лимит заявок на событие с id: {}", eventId);
            throw new ConflictException(String.format("Уже достигнут лимит предела заявок на событие с id= %d",
                    eventId));
        }

        LinkedHashMap<Long, ParticipationRequest> requestsMap = requestRepository.findAllByIdIn(requestIds)
                .stream()
                .sorted(Comparator.comparing(ParticipationRequest::getCreated))
                .collect(Collectors.toMap(
                        ParticipationRequest::getId,
                        Function.identity(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        if (requestsMap.values().stream().anyMatch(request -> request.getStatus() != RequestStatus.PENDING)) {
            log.error("Некоторые запросы имеют статус, отличный от PENDING");
            throw new ConflictException("У всех запросов должен быть статус: PENDING");
        }

        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();

        long confirmedCount = limit -
                              requestRepository.countByEventIdAndStatusEquals(eventId, RequestStatus.CONFIRMED);

        requestsMap.values().forEach(request -> {
            if (status == RequestStatus.REJECTED) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(requestMapper.toParticipationRequestDto(request));
                log.info("Заявка id {} отклонена", request.getId());
            } else {
                if (confirmedRequests.size() < confirmedCount) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests.add(requestMapper.toParticipationRequestDto(request));
                    log.info("Заявка id {} подтверждена", request.getId());
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(requestMapper.toParticipationRequestDto(request));
                    log.info("Заявка id {} отклонена из-за превышения лимита", request.getId());
                }
            }
        });

        result.getConfirmedRequests().addAll(confirmedRequests);
        result.getRejectedRequests().addAll(rejectedRequests);

        log.info("Сохранение статусов запросов");

        requestsMap.values().forEach(request ->
                log.info("Request id: {} New Status: {}", request.getId(), request.getStatus())
        );

        requestRepository.saveAll(requestsMap.values());

        return result;
    }

    @Override
    public Map<Long, List<ParticipationRequestDto>> prepareConfirmedRequests(List<Long> eventIds) {
        log.info("Находим список подтверждённых запросов для переданных id событий.");

        List<ParticipationRequestDto> confirmedRequests = requestMapper
                .toParticipationRequestDtos(requestRepository.findConfirmedRequestsByEventIds(eventIds, RequestStatus.CONFIRMED));

        Map<Long, List<ParticipationRequestDto>> result = new HashMap<>();

        for (ParticipationRequestDto request : confirmedRequests) {
            Long eventId = request.getEventId();
            List<ParticipationRequestDto> list = result.get(eventId);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(request);
            result.put(eventId, list);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean checkRegistration(Long eventId, Long userId) {
        log.info("Запрос в сервис для проверки регистрации");
        Optional<ParticipationRequest> request = requestRepository.findByEventIdAndRequesterId(eventId, userId);
        return request.isPresent();
    }


    private ParticipationRequest validateRequest(Long requesterId, Long eventId) {
        validateUserExist(requesterId);
        EventFullDto event = validateEventExist(eventId);

        validateNotExistsByEventIdAndRequesterId(eventId, requesterId);
        if (event.getInitiator().equals(requesterId)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        long limit = event.getParticipantLimit();

        if (limit > 0 &&
            requestRepository.countByEventIdAndStatusEquals(eventId, RequestStatus.CONFIRMED) >= limit) {
            throw new ConflictException("Достигнут лимит запросов на участие");
        }

        ParticipationRequest participationRequest = new ParticipationRequest();
        participationRequest.setRequesterId(requesterId);
        participationRequest.setEventId(eventId);
        participationRequest.setCreated(LocalDateTime.now());

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            participationRequest.setStatus(RequestStatus.CONFIRMED);
        } else {
            participationRequest.setStatus(RequestStatus.PENDING);
        }
        return participationRequest;
    }

    private void validateUserExist(Long userId) {
        if (userId == null) {
            throw new BadRequestException("id пользователя не может быть null");
        }

        try {
            userClient.getUserById(userId);
        } catch (FeignException.FeignClientException e) {
            log.warn("Клиентская ошибка (4xx) при получении пользователя id {}: {}", userId, e.getMessage());
            throw new UserOperationFailedException(String.format("Ошибка при получении пользователя по id %d: %s", userId, e.getMessage()));
        } catch (FeignException e) {
            log.error("Серверная ошибка (5xx) при получении пользователя id {}: {}", userId, e.getMessage(), e);
            throw new UserOperationFailedException(String.format("user-service недоступен для пользователя с id %d", userId));
        }
    }

    private EventFullDto validateEventExist(Long eventId) {
        try {
            return eventClient.getEvent(eventId);
        } catch (FeignException.FeignClientException e) {
            log.warn("Клиентская ошибка (4xx) при получении события: {}", eventId);
            throw new EventOperationFailedException(String.format("Ошибка при получении события: %d", eventId));
        } catch (FeignException e) {
            log.error("Серверная ошибка (5xx) при получении события: {}", eventId);
            throw new EventOperationFailedException(String.format("event-service недоступен для события: %d", eventId));
        }
    }

    private void validateNotExistsByEventIdAndRequesterId(Long eventId, Long requesterId) {
        requestRepository.findByEventIdAndRequesterId(eventId, requesterId)
                .ifPresent(request -> {
                    throw new DuplicatedDataException("Нельзя добавить повторный запрос для этого события");
                });
    }

    private ParticipationRequest validateRequestExist(Long requesterId, Long requestId) {
        ParticipationRequest participationRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Запрос на событие с id= " +
                                                                       "%d не найден.", requestId)));
        if (!participationRequest.getRequesterId().equals(requesterId)) {
            throw new ConflictException(String.format("Данный запрос с id= %d " +
                                                      "не принадлежит пользователю c id= %d", requestId, requesterId));
        }

        return participationRequest;
    }
}
