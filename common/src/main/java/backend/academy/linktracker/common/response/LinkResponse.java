package backend.academy.linktracker.common.response;

public record LinkResponse(
    long id,
    String url,
    String[] tags,
    String[] filters
) {}
