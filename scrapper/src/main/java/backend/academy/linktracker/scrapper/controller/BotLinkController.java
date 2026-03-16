package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.contracts.dto.request.AddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.LinkResponse;
import backend.academy.linktracker.contracts.dto.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.service.BotLinkService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BotLinkController {

    private final BotLinkService botLinkService;

    @PostMapping("/links")
    public void addLink(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody AddLinkRequest addLinkRequest) {
        log.atInfo().addKeyValue("chat_id", chatId).log("Запрос на добавление ссылки");

        botLinkService.addLink(chatId, addLinkRequest);
    }

    @DeleteMapping("/links")
    public void deleteLink(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody RemoveLinkRequest removeLinkRequest) {
        log.atInfo().addKeyValue("chat_id", chatId).log("Запрос на удаление ссылки");

        botLinkService.deleteLink(chatId, removeLinkRequest);
    }

    @GetMapping("/links")
    public ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") long chatId) {
        List<Subscription> subscriptionList = botLinkService.getSubscriptionsByChatId(chatId);

        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue("links_count", subscriptionList.size())
                .log("Запрос на получение списка ссылок");

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
