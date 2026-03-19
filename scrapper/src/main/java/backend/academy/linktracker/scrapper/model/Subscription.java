package backend.academy.linktracker.scrapper.model;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Subscription {
    private final Chat chat;
    private final Link link;
    private final List<String> tags;
    private final List<String> filters;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(chat, that.chat) && Objects.equals(link, that.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chat, link);
    }
}
