package interaction.api.feign.contract;

import interaction.api.dto.event.EventFullDto;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface EventContract {
    @GetMapping("/users/{userId}/events/{eventId}")
    EventFullDto getEventByEventIdAndUserId(@PathVariable @Positive Long userId,
                                                   @PathVariable @Positive Long eventId);

    @GetMapping("/admin/events/{eventId}")
    EventFullDto getEvent(@PathVariable @Positive Long eventId);
}