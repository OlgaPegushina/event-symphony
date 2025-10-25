package event.service.compilation.dto;

import interaction.api.dto.event.EventShortDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationDto {
    Set<EventShortDto> events;
    Long id;
    Boolean pinned;
    String title;
}
