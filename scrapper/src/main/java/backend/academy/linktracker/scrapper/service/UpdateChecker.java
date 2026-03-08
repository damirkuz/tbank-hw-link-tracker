package backend.academy.linktracker.scrapper.service;


import backend.academy.linktracker.scrapper.client.tracked.BaseTrackedClient;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateChecker {

    private final Map<TrackedResource, BaseTrackedClient> clients;

    @Scheduled(fixedRate = 10000) // 10 секунд
    public void getUpdates() {




    }

}
