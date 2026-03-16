package backend.academy.linktracker.scrapper.controller.grpc;

import backend.academy.linktracker.contracts.dto.request.AddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.LinkResponse;
import backend.academy.linktracker.contracts.dto.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.service.BotChatService;
import backend.academy.linktracker.scrapper.service.BotLinkService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
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
    public void addLink(long chatId, AddLinkRequest addLinkRequest) {
        botLinkService.addLink(chatId, addLinkRequest);
    }

    @Override
    public void deleteLink(long chatId, RemoveLinkRequest removeLinkRequest) {
        botLinkService.deleteLink(chatId, removeLinkRequest);
    }

    @Override
    public ListLinksResponse getLinks(long chatId) {
        List<Subscription> subscriptionList = botLinkService.getSubscriptionsByChatId(chatId);

        LinkResponse[] linkResponses = new LinkResponse[subscriptionList.size()];
        int count = 0;

        for (Subscription subscription : subscriptionList) {
            LinkResponse linkResponse =
                    new LinkResponse(count + 1, subscription.getLink().getUri(), subscription.getTags(), null);
            linkResponses[count++] = linkResponse;
        }

        return new ListLinksResponse(linkResponses, linkResponses.length);
    }
}
