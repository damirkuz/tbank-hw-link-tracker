package backend.academy.linktracker.contracts.dto.response;

import java.util.List;

public record CommonListLinksResponse(List<CommonLinkResponse> links, Integer size) {}
