package backend.academy.linktracker.scrapper.client.provider;

import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.time.Instant;

public interface BaseTrackedClient {
    Instant getLastUpdate(String link);

    TrackedResource getTrackedResource();
}
