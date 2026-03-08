package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.common.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.common.exception.ChatNotFoundException;
import backend.academy.linktracker.common.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.common.exception.LinkNotFoundException;
import backend.academy.linktracker.common.response.ApiErrorResponse;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ChatAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleChatAlreadyExists(ChatAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(toResponse("Чат уже существует", HttpStatus.CONFLICT, e));
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleChatNotFound(ChatNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(toResponse("Чат не существует или ссылка не найдена", HttpStatus.NOT_FOUND, e));
    }

    @ExceptionHandler(LinkAlreadyTrackedException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkAlreadyTracked(LinkAlreadyTrackedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(toResponse("Ссылка уже отслеживается", HttpStatus.CONFLICT, e));
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkNotFound(LinkNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(toResponse("Чат не существует или ссылка не найдена", HttpStatus.NOT_FOUND, e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOther(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(toResponse("Внутренняя ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR, e));
    }

    private ApiErrorResponse toResponse(String description, HttpStatus status, Exception e) {
        String[] stacktrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .toArray(String[]::new);

        return new ApiErrorResponse(
                description, String.valueOf(status.value()), e.getClass().getSimpleName(), e.getMessage(), stacktrace);
    }
}
