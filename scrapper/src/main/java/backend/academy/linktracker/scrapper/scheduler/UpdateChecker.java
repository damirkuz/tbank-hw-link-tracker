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
import java.time.Clock;
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
    private final Clock clock;

    @Scheduled(fixedRateString = "${scheduler.interval}")
    public void getUpdates() {
        OffsetDateTime before = OffsetDateTime.now(clock);
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
        OffsetDateTime nextCheckAt = OffsetDateTime.now(clock).plus(Duration.ofMillis(schedulerProperties.interval()));

        if (previousLastUpdate != null && actualLastUpdate.isAfter(previousLastUpdate)) {
            if (sendLinkUpdate(link, previousLastUpdate, actualLastUpdate)) {
                linkRepository.updateCheckState(link.getId(), actualLastUpdate, nextCheckAt);
            } else {
                linkRepository.updateNextCheckAt(link.getId(), nextCheckAt);
            }
            return;
        }

        Instant persistedLastUpdate = previousLastUpdate == null || actualLastUpdate.isAfter(previousLastUpdate)
                ? actualLastUpdate
                : previousLastUpdate;
        linkRepository.updateCheckState(link.getId(), persistedLastUpdate, nextCheckAt);
    }

    private boolean sendLinkUpdate(Link link, Instant previousLastUpdate, Instant actualLastUpdate) {
        List<Chat> chats = subscriptionRepository.getAllChatsByLink(link);
        List<Long> tgChatIds = chats.stream().map(Chat::getId).toList();

        CommonLinkUpdate commonLinkUpdate =
                new CommonLinkUpdate(link.getId(), link.getUri(), "Произошло обновление", tgChatIds);

        try {
            botClient.sendUpdate(commonLinkUpdate);
        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("resource", link.getTrackedResource())
                    .addKeyValue("uri", link.getUri())
                    .addKeyValue("previous_update", previousLastUpdate)
                    .addKeyValue("current_update", actualLastUpdate)
                    .addKeyValue("notified_chats", tgChatIds.size())
                    .log("Не удалось отправить уведомление, lastUpdate не будет продвинут");
            return false;
        }

        log.atInfo()
                .addKeyValue("resource", link.getTrackedResource())
                .addKeyValue("uri", link.getUri())
                .addKeyValue("previous_update", previousLastUpdate)
                .addKeyValue("current_update", actualLastUpdate)
                .addKeyValue("notified_chats", tgChatIds.size())
                .log("Обнаружено обновление ссылки");
        return true;
    }

    public Instant getLastUpdate(URI uri, TrackedResource trackedResource) {
        return clients.get(trackedResource).getLastUpdate(uri);
    }
}
