package stats.dto.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import static stats.dto.constant.Const.DATE_TIME_FORMAT;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EndpointHitDto {
    Long id;

    @NotBlank(message = "Параметр app не должен быть пустым")
    @Size(max = 200, message = "Слишком длинный параметр app")
    String app;

    @NotBlank(message = "URI не может быть пустым")
    @Size(max = 200, message = "Слишком длинный URI: MAX = 200")
    @Pattern(regexp = "^/.*", message = "URI должен начинаться со слеша")
    String uri;

    @NotBlank(message = "Ip адрес не может быть пустым")
    String ip;

    @NotNull(message = "Поле timestamp не может быть пустым")
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime timestamp;
}