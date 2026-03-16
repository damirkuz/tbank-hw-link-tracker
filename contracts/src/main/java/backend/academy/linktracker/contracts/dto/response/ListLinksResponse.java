package backend.academy.linktracker.contracts.dto.response;

public record ListLinksResponse(LinkResponse[] links, int size) {}
