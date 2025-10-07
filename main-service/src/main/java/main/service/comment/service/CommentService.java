package main.service.comment.service;

import main.service.comment.dto.CommentDto;
import main.service.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto create(Long userId, Long eventId, NewCommentDto newCommentDto);

    List<CommentDto> findAllByEvent(Long eventId, Integer from, Integer size);

    CommentDto update(Long userId, Long eventId, Long commentId, NewCommentDto newCommentDto);

    void delete(Long userId, Long eventId, Long commentId);

    CommentDto adminUpdate(Long eventId, Long commentId, NewCommentDto newCommentDto);

    void adminDelete(Long eventId, Long commentId);

    CommentDto findByEventAndCommentId(Long eventId, Long commentId);
}
