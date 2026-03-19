package backend.academy.linktracker.contracts.dto.mapper;

import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonLinkResponse;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.scrapper.generated.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.generated.dto.LinkResponse;
import backend.academy.linktracker.scrapper.generated.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.generated.dto.RemoveLinkRequest;

public final class ScrapperHttpMapper {

    private ScrapperHttpMapper() {}

    public static AddLinkRequest toAddLinkRequest(CommonAddLinkRequest request) {
        AddLinkRequest addLinkRequest = new AddLinkRequest();
        addLinkRequest.setLink(request.uri());
        addLinkRequest.setTags(request.tags());
        addLinkRequest.setFilters(request.filters());
        return addLinkRequest;
    }

    public static CommonAddLinkRequest fromAddLinkRequest(AddLinkRequest request) {
        return new CommonAddLinkRequest(request.getLink(), request.getTags(), request.getFilters());
    }

    public static RemoveLinkRequest toRemoveLinkRequest(CommonRemoveLinkRequest request) {
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest();
        removeLinkRequest.setLink(request.uri());
        return removeLinkRequest;
    }

    public static CommonRemoveLinkRequest fromRemoveLinkRequest(RemoveLinkRequest request) {
        return new CommonRemoveLinkRequest(request.getLink());
    }

    public static LinkResponse toLinkResponse(CommonLinkResponse response) {
        LinkResponse linkResponse = new LinkResponse();
        linkResponse.setUrl(response.url());
        linkResponse.setId(response.id());
        linkResponse.setTags(response.tags());
        linkResponse.setFilters(response.filters());
        return linkResponse;
    }

    public static CommonLinkResponse fromLinkResponse(LinkResponse response) {
        return new CommonLinkResponse(response.getId(), response.getUrl(), response.getTags(), response.getFilters());
    }

    public static ListLinksResponse toListLinksResponse(CommonListLinksResponse response) {
        ListLinksResponse listLinksResponse = new ListLinksResponse();
        listLinksResponse.setLinks(response.links().stream()
                .map(ScrapperHttpMapper::toLinkResponse)
                .toList());

        listLinksResponse.setSize(response.size());

        return listLinksResponse;
    }

    public static CommonListLinksResponse fromListLinksResponse(ListLinksResponse response) {
        return new CommonListLinksResponse(
                response.getLinks().stream()
                        .map(ScrapperHttpMapper::fromLinkResponse)
                        .toList(),
                response.getSize());
    }
}
