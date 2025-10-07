package main.service.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.service.location.LocationDto;

import java.time.LocalDateTime;

import static stats.dto.constant.Const.DATE_TIME_FORMAT;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {

    @Size(min = 20, max = 2000, message = "Поле annotation должно быть от 20 до 2000 символов")
    @NotBlank(message = "Поле annotation не может быть пустым")
    String annotation;

    @NotNull(message = "Поле category не может быть пустым")
    Long category;

    @Size(min = 20, max = 7000, message = "Поле description должно быть от 20 до 7000 символов")
    @NotBlank(message = "Поле description не может быть пустым")
    String description;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    @NotNull(message = "Поле eventDate не может быть пустым")
    @Future(message = "Поле eventDate должно быть в будущем")
    LocalDateTime eventDate;

    @Builder.Default
    Boolean paid = false;

    @PositiveOrZero(message = "Лимит участников должен быть положительным или равен нулю.")
    @Builder.Default
    Long participantLimit = 0L;

    @Builder.Default
    Boolean requestModeration = true;

    @Size(min = 3, max = 120, message = "Поле title должно быть от 3 до 120 символов")
    @NotBlank(message = "Поле title не может быть пустым")
    String title;

    @NotNull(message = "Поле location не может быть пустым")
    @JsonProperty("location")
    LocationDto locationDto;
}
