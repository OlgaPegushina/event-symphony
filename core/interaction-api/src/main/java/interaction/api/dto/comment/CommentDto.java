package interaction.api.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import static interaction.api.utility.AppConstants.DATE_TIME_FORMAT;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {
    Long id;
    String message;
    Long authorId;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime created;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime updated;
}
