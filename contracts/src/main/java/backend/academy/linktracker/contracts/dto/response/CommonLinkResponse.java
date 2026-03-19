package backend.academy.linktracker.contracts.dto.response;

import java.net.URI;
import java.util.List;

public record CommonLinkResponse(Long id, URI url, List<String> tags, List<String> filters) {}
