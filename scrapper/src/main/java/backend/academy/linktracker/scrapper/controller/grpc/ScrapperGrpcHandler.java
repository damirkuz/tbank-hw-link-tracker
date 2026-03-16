package backend.academy.linktracker.scrapper.controller.grpc;

import backend.academy.linktracker.contracts.dto.request.AddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.ListLinksResponse;

public interface ScrapperGrpcHandler {
    void registerChat(long chatId);

    void deleteChat(long chatId);

    void addLink(long chatId, AddLinkRequest addLinkRequest);

    void deleteLink(long chatId, RemoveLinkRequest removeLinkRequest);

    ListLinksResponse getLinks(long chatId);
}
