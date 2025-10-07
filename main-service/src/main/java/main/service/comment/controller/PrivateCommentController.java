package main.service.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import main.service.comment.dto.CommentDto;
import main.service.comment.dto.NewCommentDto;
import main.service.comment.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/comments")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class PrivateCommentController {
    CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable @NotNull @Positive Long userId,
                                    @PathVariable @NotNull @Positive Long eventId,
                                    @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Сохранение комментария для пользователя id: {} и события id: {}", userId, eventId);
        return commentService.create(userId, eventId, newCommentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable @NotNull @Positive Long userId,
                                    @PathVariable @NotNull @Positive Long eventId,
                                    @PathVariable @NotNull @Positive Long commentId,
                                    @RequestBody @Valid NewCommentDto updateCommentDto) {
        log.info("Обновление комментария id: {} для пользователя id: {} и события id: {}", commentId, userId, eventId);
        return commentService.update(userId, eventId, commentId, updateCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @NotNull @Positive Long userId,
                              @PathVariable @NotNull @Positive Long eventId,
                              @PathVariable @NotNull @Positive Long commentId) {
        log.info("Удаление комментария id: {} для пользователя id: {} и события id: {}", commentId, userId, eventId);
        commentService.delete(userId, eventId, commentId);
    }
}
