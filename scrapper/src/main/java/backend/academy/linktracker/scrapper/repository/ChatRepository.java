package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.common.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.common.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.model.Chat;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ChatRepository {

    private final Set<Chat> chats = ConcurrentHashMap.newKeySet();

    public void registerChat(Chat chat) {
        boolean added = chats.add(chat);

        if (!added) {
            throw new ChatAlreadyExistsException();
        }
    }

    public void deleteChat(Chat chat) {
        boolean deleted = chats.remove(chat);
        if (!deleted) {
            throw new ChatNotFoundException();
        }
    }

    public Chat getChat(long chatId) {
        Chat chat = new Chat(chatId);
        boolean hasChat = chats.contains(chat);
        if (!hasChat) {
            throw new ChatNotFoundException();
        }
        return chat;
    }
}
