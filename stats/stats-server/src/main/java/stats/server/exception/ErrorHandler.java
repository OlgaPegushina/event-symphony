package stats.server.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice()
@SuppressWarnings("unused")
public class ErrorHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse validationExceptionHandle(Exception e) {
        return new ExceptionResponse("Ошибка валидации", e.getMessage());
    }

    @Getter
    @AllArgsConstructor
    public static class ExceptionResponse {
        String error;
        String description;
    }
}