package backend.academy.linktracker.scrapper.model;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Filter {
    private Long id;
    private final Chat chat;
    private String value;

    public Filter(Chat chat, String value) {
        this.chat = chat;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Filter filter = (Filter) o;
        return Objects.equals(chat, filter.chat) && Objects.equals(value, filter.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chat, value);
    }
}
