package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.tracked.BaseTrackedClient;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateChecker {

    private final Map<TrackedResource, BaseTrackedClient> clients;

    // при добавлении ссылки можно не проверять её последний апдейт, просто сохранять null
    // маловероятно, что обновление прилетит до следующего планового обхода всех ссылок

    @Scheduled(fixedRate = 10000) // 10 секунд
    public void getUpdates() {}

    public Instant getLastUpdate(String uri, TrackedResource trackedResource) {
        return clients.get(trackedResource).getLastUpdate(uri);
    }
}
