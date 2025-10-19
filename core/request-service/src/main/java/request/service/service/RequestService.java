package request.service.service;

import interaction.api.dto.request.EventRequestStatusUpdateRequestDto;
import interaction.api.dto.request.EventRequestStatusUpdateResultDto;
import interaction.api.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto createRequest(Long requesterId, Long eventId);

    ParticipationRequestDto cancelRequest(Long requesterId, Long requestId);

    List<ParticipationRequestDto> getRequests(Long requesterId);

    List<ParticipationRequestDto> getCurrentUserEventRequests(Long initiatorId, Long eventId);

    EventRequestStatusUpdateResultDto updateParticipationRequestsStatus(Long initiatorId, Long eventId,
                                                                        EventRequestStatusUpdateRequestDto eventRequestStatusUpdateRequestDto);
}
