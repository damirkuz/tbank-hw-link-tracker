package backend.academy.linktracker.contracts.dto.request;

import java.net.URI;
import java.util.List;

public record CommonLinkUpdate(Long id, URI url, String description, List<Long> tgChatIds) {}
