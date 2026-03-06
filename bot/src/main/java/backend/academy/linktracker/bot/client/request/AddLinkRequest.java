package backend.academy.linktracker.bot.client.request;

public record AddLinkRequest(
    String uri,
    String[] tags,
    String[] filters
) {}
