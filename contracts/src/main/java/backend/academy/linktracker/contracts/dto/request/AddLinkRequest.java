package backend.academy.linktracker.contracts.dto.request;

public record AddLinkRequest(String uri, String[] tags, String[] filters) {}
