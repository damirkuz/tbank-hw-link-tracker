package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.common.request.LinkUpdate;
import backend.academy.linktracker.scrapper.client.module.BotGateway;
import backend.academy.linktracker.scrapper.client.tracked.BaseTrackedClient;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateChecker {

    private final Map<TrackedResource, BaseTrackedClient> clients;
    private final LinkRepository linkRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BotGateway botClient;

    @Scheduled(fixedRate = 10000)
    public void getUpdates() {
        List<Link> links = linkRepository.getLinks();
        int id = 0;

        for (Link link : links) {
            Instant lastUpdate = getLastUpdate(link.getUri(), link.getTrackedResource());

            if (link.getLastUpdate() == null) {
                link.setLastUpdate(lastUpdate);

                continue;
            }

            if (lastUpdate.isAfter(link.getLastUpdate())) {
                Instant previousUpdate = link.getLastUpdate();
                link.setLastUpdate(lastUpdate);

                List<Chat> chats = subscriptionRepository.getAllChatsByLink(link);
                Long[] tgChatIds = chats.stream().map(Chat::getChatId).toArray(Long[]::new);

                LinkUpdate linkUpdate = new LinkUpdate(++id, link.getUri(), "Произошло обновление", tgChatIds);

                botClient.sendUpdate(linkUpdate);

                log.atInfo()
                        .addKeyValue("resource", link.getTrackedResource())
                        .addKeyValue("uri", link.getUri())
                        .addKeyValue("previous_update", previousUpdate)
                        .addKeyValue("current_update", lastUpdate)
                        .addKeyValue("notified_chats", tgChatIds.length)
                        .log("Обнаружено обновление ссылки");
            }
        }
    }

    public Instant getLastUpdate(String uri, TrackedResource trackedResource) {
        return clients.get(trackedResource).getLastUpdate(uri);
    }
}
