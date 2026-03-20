package backend.academy.linktracker.bot.client.protocol;

import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;

public interface ScrapperGateway {
    void registerChat(long chatId) throws ChatAlreadyExistsException;

    void deleteChat(long chatId) throws ChatNotFoundException;

    void addLink(long chatId, CommonAddLinkRequest commonAddLinkRequest)
            throws ChatNotFoundException, LinkAlreadyTrackedException;

    void deleteLink(long chatId, CommonRemoveLinkRequest commonRemoveLinkRequest) throws ChatNotFoundException;

    CommonListLinksResponse getLinks(long chatId) throws ChatNotFoundException;
}
