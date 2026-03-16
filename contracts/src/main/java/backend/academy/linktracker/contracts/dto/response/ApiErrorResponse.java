package backend.academy.linktracker.contracts.dto.response;

public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, String[] stacktrace) {}
