package backend.academy.linktracker.scrapper.model;

import java.util.Objects;

public record Link(String uri, String[] tags) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(uri(), link.uri());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uri());
    }
}
