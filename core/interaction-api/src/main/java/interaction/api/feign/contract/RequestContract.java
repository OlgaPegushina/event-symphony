package interaction.api.feign.contract;

import interaction.api.dto.request.EventRequestStatusUpdateRequestDto;
import interaction.api.dto.request.EventRequestStatusUpdateResultDto;
import interaction.api.dto.request.ParticipationRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface RequestContract {
    @GetMapping("/users/{userId}/requests/{eventId}")
    List<ParticipationRequestDto> getCurrentUserEventRequests(@PathVariable("userId") @Positive Long initiatorId,
                                                                     @PathVariable("eventId") @Positive Long eventId);

    @PatchMapping("/users/{userId}/requests/{eventId}/requests")
    EventRequestStatusUpdateResultDto updateParticipationRequestsStatus(
            @PathVariable("userId") @Positive Long initiatorId,
            @PathVariable("eventId") @Positive Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequestDto e);
}
