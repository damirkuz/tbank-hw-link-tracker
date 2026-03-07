package backend.academy.linktracker.common.response;

public record ApiErrorResponse (
    String description,
    String code,
    String exceptionName,
    String exceptionMessage,
    String[] stacktrace
) {}
