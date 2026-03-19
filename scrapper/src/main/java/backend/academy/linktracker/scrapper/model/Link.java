package backend.academy.linktracker.scrapper.model;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Link {
    private Long id;
    private final URI uri;
    private final TrackedResource trackedResource;
    private Instant lastUpdate;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return getTrackedResource() == link.getTrackedResource() && Objects.equals(getUri(), link.getUri());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTrackedResource(), getUri());
    }
}
