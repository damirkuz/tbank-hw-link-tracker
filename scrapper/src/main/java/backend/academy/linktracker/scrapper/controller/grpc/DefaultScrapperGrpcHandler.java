package backend.academy.linktracker.scrapper.controller.grpc;

import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.scrapper.service.BotChatService;
import backend.academy.linktracker.scrapper.service.BotLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "bot", name = "transport", havingValue = "grpc")
public class DefaultScrapperGrpcHandler implements ScrapperGrpcHandler {

    private final BotChatService botChatService;
    private final BotLinkService botLinkService;

    @Override
    public void registerChat(long chatId) {
        botChatService.registerChat(chatId);
    }

    @Override
    public void deleteChat(long chatId) {
        botChatService.deleteChat(chatId);
    }

    @Override
    public void addLink(long chatId, CommonAddLinkRequest commonAddLinkRequest) {
        botLinkService.addLink(chatId, commonAddLinkRequest);
    }

    @Override
    public void deleteLink(long chatId, CommonRemoveLinkRequest commonRemoveLinkRequest) {
        botLinkService.deleteLink(chatId, commonRemoveLinkRequest);
    }

    @Override
    public CommonListLinksResponse getLinks(long chatId) {
        return botLinkService.getLinks(chatId);
    }
}
