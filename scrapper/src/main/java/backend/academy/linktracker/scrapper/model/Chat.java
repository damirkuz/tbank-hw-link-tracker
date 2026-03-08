package backend.academy.linktracker.scrapper.model;

import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Chat {
    private final long chatId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return chatId == chat.chatId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(chatId);
    }
}
