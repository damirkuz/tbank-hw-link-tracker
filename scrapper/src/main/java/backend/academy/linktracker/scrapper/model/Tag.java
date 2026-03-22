package backend.academy.linktracker.scrapper.model;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tag {
    private Long id;
    private final Chat chat;
    private String name;

    public Tag(Chat chat, String name) {
        this.chat = chat;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tag tag = (Tag) o;
        return Objects.equals(chat, tag.chat) && Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chat, name);
    }
}
