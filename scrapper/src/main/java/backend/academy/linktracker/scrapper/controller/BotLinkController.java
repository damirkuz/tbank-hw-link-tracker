package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.common.request.AddLinkRequest;
import backend.academy.linktracker.common.request.RemoveLinkRequest;
import backend.academy.linktracker.common.response.LinkResponse;
import backend.academy.linktracker.common.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.service.BotLinkService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BotLinkController {

    private final BotLinkService botLinkService;

    @PostMapping("/links")
    public void addLink(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody AddLinkRequest addLinkRequest) {
        botLinkService.addLink(chatId, addLinkRequest);
    }

    @DeleteMapping("/links")
    public void deleteLink(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody RemoveLinkRequest removeLinkRequest) {
        botLinkService.deleteLink(chatId, removeLinkRequest);
    }

    @GetMapping("/links")
    public ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") long chatId) {
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
