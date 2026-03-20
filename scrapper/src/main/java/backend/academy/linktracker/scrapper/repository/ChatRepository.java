package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Chat;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ChatRepository {

    private final Set<Chat> chats = ConcurrentHashMap.newKeySet();

    public boolean registerChat(Chat chat) {
        return chats.add(chat);
    }

    public boolean deleteChat(Chat chat) {
        return chats.remove(chat);
    }

    public Optional<Chat> findById(long chatId) {
        Chat chat = new Chat(chatId);
        return chats.contains(chat) ? Optional.of(chat) : Optional.empty();
    }
}
