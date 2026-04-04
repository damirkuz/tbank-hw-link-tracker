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
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
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
        OffsetDateTime before = OffsetDateTime.now();
        long lastSeenId = 0L;
        int batchSize = schedulerProperties.batchSize();

        while (true) {
            List<Link> batch = linkRepository.findLinksForUpdate(before, lastSeenId, batchSize);
            if (batch.isEmpty()) {
                break;
            }

            for (Link link : batch) {
                processLink(link);
            }

            lastSeenId = batch.getLast().getId();
        }
    }

    private void processLink(Link link) {
        Instant actualLastUpdate = getLastUpdate(link.getUri(), link.getTrackedResource());
        Instant previousLastUpdate = link.getLastUpdate();

        Instant persistedLastUpdate = previousLastUpdate == null || actualLastUpdate.isAfter(previousLastUpdate)
                ? actualLastUpdate
                : previousLastUpdate;

        OffsetDateTime nextCheckAt = OffsetDateTime.now().plus(Duration.ofMillis(schedulerProperties.interval()));

        linkRepository.updateCheckState(link.getId(), persistedLastUpdate, nextCheckAt);

        if (previousLastUpdate != null && actualLastUpdate.isAfter(previousLastUpdate)) {
            List<Chat> chats = subscriptionRepository.getAllChatsByLink(link);
            List<Long> tgChatIds = chats.stream().map(Chat::getId).toList();

            CommonLinkUpdate commonLinkUpdate =
                    new CommonLinkUpdate(link.getId(), link.getUri(), "Произошло обновление", tgChatIds);

            botClient.sendUpdate(commonLinkUpdate);

            log.atInfo()
                    .addKeyValue("resource", link.getTrackedResource())
                    .addKeyValue("uri", link.getUri())
                    .addKeyValue("previous_update", previousLastUpdate)
                    .addKeyValue("current_update", actualLastUpdate)
                    .addKeyValue("notified_chats", tgChatIds.size())
                    .log("Обнаружено обновление ссылки");
        }
    }

    public Instant getLastUpdate(URI uri, TrackedResource trackedResource) {
        return clients.get(trackedResource).getLastUpdate(uri);
    }
}
