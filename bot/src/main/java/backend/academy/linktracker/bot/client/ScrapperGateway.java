package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;

public interface ScrapperGateway {
    void registerChat(long chatId);

    void deleteChat(long chatId);

    void addLink(long chatId, CommonAddLinkRequest commonAddLinkRequest);

    void deleteLink(long chatId, CommonRemoveLinkRequest commonRemoveLinkRequest);

    CommonListLinksResponse getLinks(long chatId);
}
