package main.service.exception;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice()
@SuppressWarnings("unused")
public class ErrorHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            ValidationException.class,
            HandlerMethodValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validationExceptionHandle(Exception e) {
        log.error("Validation error: ", e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Ошибка валидации")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(DuplicatedDataException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError repositoryDuplicatedDataExceptionHandle(Exception e) {
        log.error("Duplicated Data Exception error: ", e);
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Ресурс дублируется")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError repositoryNotFoundExceptionHandle(Exception e) {
        log.error("Not Found Exception error: ", e);
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .reason("Запрашиваемый ресурс не найден")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError exceptionHandle(Exception e) {
        log.error("CONFLICT error: ", e);
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Нарушено ограничение целостности")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequestExceptionHandle(Exception e) {
        log.error("Bad Request Exception error: ", e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Некорректный запрос")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError internalServerExceptionHandle(Exception e) {
        log.error("Internal Server Exception error: ", e);
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .reason("Ошибка сервера")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}