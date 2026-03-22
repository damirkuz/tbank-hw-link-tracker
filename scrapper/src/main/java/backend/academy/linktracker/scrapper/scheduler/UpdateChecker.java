package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.contracts.dto.request.CommonLinkUpdate;
import backend.academy.linktracker.scrapper.client.protocol.BotGateway;
import backend.academy.linktracker.scrapper.client.provider.BaseTrackedClient;
import backend.academy.linktracker.scrapper.config.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.net.URI;
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
    private final SchedulerProperties schedulerProperties;

    @Scheduled(fixedRateString = "${scheduler.interval}")
    public void getUpdates() {
        List<Link> links = linkRepository.getLinks();

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
                List<Long> tgChatIds = chats.stream().map(Chat::getChatId).toList();

                CommonLinkUpdate commonLinkUpdate =
                        new CommonLinkUpdate(link.getId(), link.getUri(), "Произошло обновление", tgChatIds);

                botClient.sendUpdate(commonLinkUpdate);

                log.atInfo()
                        .addKeyValue("resource", link.getTrackedResource())
                        .addKeyValue("uri", link.getUri())
                        .addKeyValue("previous_update", previousUpdate)
                        .addKeyValue("current_update", lastUpdate)
                        .addKeyValue("notified_chats", tgChatIds.size())
                        .log("Обнаружено обновление ссылки");
            }
        }
    }

    public Instant getLastUpdate(URI uri, TrackedResource trackedResource) {
        return clients.get(trackedResource).getLastUpdate(uri);
    }
}
