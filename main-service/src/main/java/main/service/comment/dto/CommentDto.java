package main.service.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.service.user.dto.UserShortDto;

import java.time.LocalDateTime;

import static stats.dto.constant.Const.DATE_TIME_FORMAT;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {
    Long id;
    String message;
    UserShortDto author;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime created;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime updated;
}
