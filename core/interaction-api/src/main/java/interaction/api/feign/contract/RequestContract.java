package interaction.api.feign.contract;

import interaction.api.dto.request.BulkRequestStatusUpdateCommand;
import interaction.api.dto.request.EventRequestStatusUpdateResultDto;
import interaction.api.dto.request.ParticipationRequestDto;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface RequestContract {
    @GetMapping("/users/{userId}/requests/{eventId}")
    List<ParticipationRequestDto> getCurrentUserEventRequests(@PathVariable("userId") @Positive Long initiatorId,
                                                                     @PathVariable("eventId") @Positive Long eventId);

    @RequestMapping(
            method = RequestMethod.PATCH,
            value = "/requests/status/bulk-update",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    EventRequestStatusUpdateResultDto updateParticipationRequestsStatus(
            @RequestBody BulkRequestStatusUpdateCommand command);

    @GetMapping("/users/requests/confirmed")
    Map<Long, List<ParticipationRequestDto>> prepareConfirmedRequests(@RequestParam List<Long> eventIds);

    @GetMapping("/users/requests/confirmed/{eventId}/{userId}")
    Boolean checkRegistration(@PathVariable Long eventId, @PathVariable Long userId);
}
