package backend.academy.linktracker.scrapper.client.provider;

import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.net.URI;
import java.time.Instant;

public interface BaseTrackedClient {
    Instant getLastUpdate(URI link);

    TrackedResource getTrackedResource();
}
