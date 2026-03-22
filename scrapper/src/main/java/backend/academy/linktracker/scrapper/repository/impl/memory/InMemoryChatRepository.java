package backend.academy.linktracker.scrapper.repository.impl.memory;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "MEMORY")
public class InMemoryChatRepository implements ChatRepository {

    private final Set<Chat> chats = ConcurrentHashMap.newKeySet();

    @Override
    public boolean registerChat(Chat chat) {
        return chats.add(chat);
    }

    @Override
    public boolean deleteChat(Chat chat) {
        return chats.remove(chat);
    }

    @Override
    public Optional<Chat> findById(long chatId) {
        Chat chat = new Chat(chatId);
        return chats.contains(chat) ? Optional.of(chat) : Optional.empty();
    }
}
