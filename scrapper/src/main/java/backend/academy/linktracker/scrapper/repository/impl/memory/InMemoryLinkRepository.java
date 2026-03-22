package backend.academy.linktracker.scrapper.repository.impl.memory;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "MEMORY")
public class InMemoryLinkRepository implements LinkRepository {

    private final AtomicLong idCounter = new AtomicLong();
    private final Map<Long, Link> linksById = new ConcurrentHashMap<>();
    private final Map<String, Long> idsByBusinessKey = new ConcurrentHashMap<>();

    @Override
    public List<Link> findLinksForUpdate(OffsetDateTime before, long lastSeenId, int limit) {
        return linksById.values().stream()
                .filter(link -> link.getId() > lastSeenId)
                .filter(link ->
                        link.getNextCheckAt() == null || !link.getNextCheckAt().isAfter(before))
                .sorted(Comparator.comparing(Link::getId))
                .limit(limit)
                .toList();
    }

    @Override
    public void addLink(Link link) {
        String key = businessKey(link);

        Long id = idsByBusinessKey.computeIfAbsent(key, ignored -> {
            long newId = idCounter.incrementAndGet();

            Link stored = new Link(link.getUri(), link.getTrackedResource());
            stored.setId(newId);
            stored.setLastUpdate(link.getLastUpdate());
            stored.setNextCheckAt(link.getNextCheckAt());

            linksById.put(newId, stored);
            return newId;
        });

        link.setId(id);
    }

    @Override
    public void updateCheckState(long linkId, Instant lastUpdate, OffsetDateTime nextCheckAt) {
        Link stored = linksById.get(linkId);
        if (stored != null) {
            stored.setLastUpdate(lastUpdate);
            stored.setNextCheckAt(nextCheckAt);
        }
    }

    @Override
    public Optional<Link> findById(long id) {
        return Optional.ofNullable(linksById.get(id));
    }

    @Override
    public Optional<Link> findByUriAndTrackedResource(URI uri, TrackedResource trackedResource) {
        Long id = idsByBusinessKey.get(businessKey(uri, trackedResource));
        return id == null ? Optional.empty() : Optional.ofNullable(linksById.get(id));
    }

    @Override
    public boolean deleteById(long id) {
        Link removed = linksById.remove(id);
        if (removed == null) {
            return false;
        }

        idsByBusinessKey.remove(businessKey(removed));
        return true;
    }

    private String businessKey(Link link) {
        return businessKey(link.getUri(), link.getTrackedResource());
    }

    private String businessKey(URI uri, TrackedResource trackedResource) {
        return trackedResource + "::" + uri;
    }
}
