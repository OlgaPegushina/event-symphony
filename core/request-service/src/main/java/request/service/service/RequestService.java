package request.service.service;

import interaction.api.dto.request.EventRequestStatusUpdateResultDto;
import interaction.api.dto.request.ParticipationRequestDto;
import interaction.api.dto.request.RequestStatusUpdateDto;

import java.util.List;
import java.util.Map;

public interface RequestService {
    ParticipationRequestDto createRequest(Long requesterId, Long eventId);

    ParticipationRequestDto cancelRequest(Long requesterId, Long requestId);

    List<ParticipationRequestDto> getRequests(Long requesterId);

    List<ParticipationRequestDto> getCurrentUserEventRequests(Long initiatorId, Long eventId);

    EventRequestStatusUpdateResultDto updateParticipationRequestsStatus(Long initiatorId, Long eventId,
                                                                        RequestStatusUpdateDto requestStatusUpdateDto);

    Map<Long, List<ParticipationRequestDto>> prepareConfirmedRequests(List<Long> eventIds);

    Boolean checkRegistration(Long eventId, Long userId);
}
