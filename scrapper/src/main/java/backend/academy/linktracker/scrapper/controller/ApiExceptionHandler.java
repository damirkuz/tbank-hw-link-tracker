package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.contracts.dto.response.CommonApiErrorResponse;
import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.contracts.exception.LinkNotFoundException;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ChatAlreadyExistsException.class)
    public ResponseEntity<@NotNull CommonApiErrorResponse> handleChatAlreadyExists(ChatAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(toResponse("Чат уже существует", HttpStatus.CONFLICT, e));
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<@NotNull CommonApiErrorResponse> handleChatNotFound(ChatNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(toResponse("Чат не существует или ссылка не найдена", HttpStatus.NOT_FOUND, e));
    }

    @ExceptionHandler(LinkAlreadyTrackedException.class)
    public ResponseEntity<@NotNull CommonApiErrorResponse> handleLinkAlreadyTracked(LinkAlreadyTrackedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(toResponse("Ссылка уже отслеживается", HttpStatus.CONFLICT, e));
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<@NotNull CommonApiErrorResponse> handleLinkNotFound(LinkNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(toResponse("Чат не существует или ссылка не найдена", HttpStatus.NOT_FOUND, e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NotNull CommonApiErrorResponse> handleOther(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(toResponse("Внутренняя ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR, e));
    }

    private CommonApiErrorResponse toResponse(String description, HttpStatus status, Exception e) {
        List<String> stacktrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();

        return new CommonApiErrorResponse(
                description, String.valueOf(status.value()), e.getClass().getSimpleName(), e.getMessage(), stacktrace);
    }
}
