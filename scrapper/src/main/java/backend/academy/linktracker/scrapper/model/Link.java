package backend.academy.linktracker.scrapper.model;

import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Link {
    private String uri;
    private String[] tags;
    private Instant lastUpdate;
    private TrackedResource trackedResource;




    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(getUri(), link.getUri());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUri());
    }
}
