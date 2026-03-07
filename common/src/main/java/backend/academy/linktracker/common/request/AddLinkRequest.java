package backend.academy.linktracker.common.request;

public record AddLinkRequest(
    String uri,
    String[] tags,
    String[] filters
) {}
