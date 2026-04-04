package backend.academy.linktracker.scrapper.repository.jpa.mapper;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.jpa.entity.ChatEntity;
import org.springframework.stereotype.Component;

@Component
public class ChatEntityMapper {

    public ChatEntity toEntity(Chat chat) {
        return new ChatEntity(chat.getId());
    }

    public Chat toDomain(ChatEntity entity) {
        return new Chat(entity.getId());
    }
}
