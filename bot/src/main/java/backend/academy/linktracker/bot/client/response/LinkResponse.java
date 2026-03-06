package backend.academy.linktracker.bot.client.response;

public record LinkResponse(
    long id,
    String url,
    String[] tags,
    String[] filters
) {}
