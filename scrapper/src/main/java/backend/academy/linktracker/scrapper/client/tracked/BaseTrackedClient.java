package backend.academy.linktracker.scrapper.client.tracked;

import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.time.Instant;

public interface BaseTrackedClient {
    Instant getLastUpdate(String link);
    TrackedResource getTrackedResource();
}
