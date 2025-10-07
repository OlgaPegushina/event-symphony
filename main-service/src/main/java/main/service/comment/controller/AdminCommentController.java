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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/events/{eventId}/comments")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class AdminCommentController {
    CommentService commentService;

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable @NotNull @Positive Long eventId,
                                    @PathVariable @NotNull @Positive Long commentId,
                                    @RequestBody @Valid NewCommentDto updateCommentDto) {
        log.info("Поступил Admin-запрос на обновление комментария id: {} для события id: {}", commentId, eventId);
        return commentService.adminUpdate(eventId, commentId, updateCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @NotNull @Positive Long eventId,
                              @PathVariable @NotNull @Positive Long commentId) {
        log.info("Поступил Admin-запрос на удаление комментария id: {} для события id: {}", commentId, eventId);
        commentService.adminDelete(eventId, commentId);
    }

    @GetMapping
    public List<CommentDto> getCommentsByEvent(@PathVariable @NotNull @Positive Long eventId,
                                               @RequestParam(name = "from", defaultValue = "0") int from,
                                               @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Получен Admin-запрос списка комментариев по событию id: {}", eventId);
        return commentService.findAllByEvent(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto findCommentByEventAndId(@PathVariable @NotNull @Positive Long eventId,
                                              @PathVariable @NotNull @Positive Long commentId) {
        log.info("Получен Admin-запрос одного комментария id: {} по событию id: {}", commentId, eventId);
        return commentService.findByEventAndCommentId(eventId, commentId);
    }
}
