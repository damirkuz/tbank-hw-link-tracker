package backend.academy.linktracker.common.response;

public record ListLinksResponse(
    LinkResponse[] links,
    int size
) {}
