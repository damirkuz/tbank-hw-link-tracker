package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {

    List<Link> findLinksForUpdate(OffsetDateTime before, long lastSeenId, int limit);

    void addLink(Link link);

    void updateCheckState(long linkId, Instant lastUpdate, OffsetDateTime nextCheckAt);

    void updateNextCheckAt(long linkId, OffsetDateTime nextCheckAt);

    Optional<Link> findById(long id);

    Optional<Link> findByUriAndTrackedResource(URI uri, TrackedResource trackedResource);

    boolean deleteById(long id);
}
