package backend.academy.linktracker.contracts.dto.response;

import java.util.List;

public record CommonApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {}
