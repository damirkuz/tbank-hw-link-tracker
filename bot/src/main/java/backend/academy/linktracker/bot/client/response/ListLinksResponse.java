package backend.academy.linktracker.bot.client.response;

public record ListLinksResponse(
    LinkResponse[] links,
    int size
) {}
