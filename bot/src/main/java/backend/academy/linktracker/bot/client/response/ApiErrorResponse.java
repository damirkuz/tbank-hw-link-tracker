package backend.academy.linktracker.bot.client.response;

public record ApiErrorResponse (
    String description,
    String code,
    String exceptionName,
    String exceptionMessage,
    String[] stacktrace
) {}
