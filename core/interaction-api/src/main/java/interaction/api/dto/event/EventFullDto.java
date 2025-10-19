package interaction.api.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import interaction.api.dto.category.CategoryDto;
import interaction.api.dto.location.LocationDto;
import interaction.api.enums.EventState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import static interaction.api.utility.AppConstants.DATE_TIME_FORMAT;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFullDto {
    Long id;
    String annotation;

    @JsonProperty("category")
    CategoryDto categoryDto;

    Long confirmedRequests;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime createdOn;

    String description;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime eventDate;

    Long initiatorId;

    @JsonProperty("location")
    LocationDto locationDto;

    Boolean paid;
    Long participantLimit;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime publishedOn;
    Boolean requestModeration;
    EventState state;
    String title;
    Long views;
}
