package backend.academy.linktracker.contracts.dto.response;

public record LinkResponse(long id, String url, String[] tags, String[] filters) {}
