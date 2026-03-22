package backend.academy.linktracker.scrapper.controller.http;

import backend.academy.linktracker.contracts.dto.response.CommonApiErrorResponse;
import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.contracts.exception.LinkNotFoundException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(ChatAlreadyExistsException.class)
    public ResponseEntity<@NotNull CommonApiErrorResponse> handleChatAlreadyExists(ChatAlreadyExistsException e) {
        return buildResponse("Чат уже существует", HttpStatus.CONFLICT, e, true);
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<@NotNull CommonApiErrorResponse> handleChatNotFound(ChatNotFoundException e) {
        return buildResponse("Чат не существует или ссылка не найдена", HttpStatus.NOT_FOUND, e, true);
    }

    @ExceptionHandler(LinkAlreadyTrackedException.class)
    public ResponseEntity<@NotNull CommonApiErrorResponse> handleLinkAlreadyTracked(LinkAlreadyTrackedException e) {
        return buildResponse("Ссылка уже отслеживается", HttpStatus.CONFLICT, e, true);
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<@NotNull CommonApiErrorResponse> handleLinkNotFound(LinkNotFoundException e) {
        return buildResponse("Чат не существует или ссылка не найдена", HttpStatus.NOT_FOUND, e, true);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NotNull CommonApiErrorResponse> handleOther(Exception e) {
        return buildResponse("Внутренняя ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR, e, false);
    }

    private ResponseEntity<@NotNull CommonApiErrorResponse> buildResponse(
            String description, HttpStatus status, Exception e, boolean expected) {
        logException(description, status, e, expected);

        return ResponseEntity.status(status).body(toResponse(description, status, e));
    }

    private void logException(String description, HttpStatus status, Exception e, boolean expected) {
        var builder = expected ? log.atWarn() : log.atError();

        if (!expected) {
            builder = builder.setCause(e);
        }

        builder.addKeyValue("layer", "rest")
                .addKeyValue("http_status", status.value())
                .addKeyValue("exception", e.getClass().getSimpleName())
                .addKeyValue("message", e.getMessage())
                .log(
                        expected
                                ? "Ожидаемая ошибка при обработке HTTP-запроса: " + description
                                : "Неожиданная ошибка при обработке HTTP-запроса");
    }

    private CommonApiErrorResponse toResponse(String description, HttpStatus status, Exception e) {
        List<String> stacktrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();

        return new CommonApiErrorResponse(
                description, String.valueOf(status.value()), e.getClass().getSimpleName(), e.getMessage(), stacktrace);
    }
}
