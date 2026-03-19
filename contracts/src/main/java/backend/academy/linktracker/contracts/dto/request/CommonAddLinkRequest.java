package backend.academy.linktracker.contracts.dto.request;

import java.net.URI;
import java.util.List;

public record CommonAddLinkRequest(URI uri, List<String> tags, List<String> filters) {}
