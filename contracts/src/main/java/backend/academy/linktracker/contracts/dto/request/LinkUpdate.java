package backend.academy.linktracker.contracts.dto.request;

public record LinkUpdate(long id, String url, String description, Long[] tgChatIds) {}
