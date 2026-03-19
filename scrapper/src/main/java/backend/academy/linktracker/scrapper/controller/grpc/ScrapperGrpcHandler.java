package backend.academy.linktracker.scrapper.controller.grpc;

import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;

public interface ScrapperGrpcHandler {
    void registerChat(long chatId);

    void deleteChat(long chatId);

    void addLink(long chatId, CommonAddLinkRequest addLinkRequest);

    void deleteLink(long chatId, CommonRemoveLinkRequest removeLinkRequest);

    CommonListLinksResponse getLinks(long chatId);
}
