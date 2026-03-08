package backend.academy.linktracker.common.request;

public record LinkUpdate(long id, String url, String description, Long[] tgChatIds) {}
