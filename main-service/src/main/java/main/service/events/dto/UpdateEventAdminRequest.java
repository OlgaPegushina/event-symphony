package main.service.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.service.events.enums.StateActionAdmin;
import main.service.location.LocationDto;

import java.time.LocalDateTime;

import static stats.dto.constant.Const.DATE_TIME_FORMAT;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventAdminRequest {

    @Size(min = 20, max = 2000, message = "Поле annotation должно быть от 20 до 2000 символов")
    String annotation;

    Long category;

    @Size(min = 20, max = 7000, message = "Поле description должно быть от 20 до 7000 символов")
    String description;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime eventDate;

    Boolean paid;

    @PositiveOrZero(message = "Лимит участников должен быть положительным или равен нулю.")
    Long participantLimit;

    Boolean requestModeration;

    @JsonProperty("stateAction")
    StateActionAdmin state;

    @Size(min = 3, max = 120, message = "Поле title должно быть от 3 до 120 символов")
    String title;

    @JsonProperty("location")
    LocationDto locationDto;
}
