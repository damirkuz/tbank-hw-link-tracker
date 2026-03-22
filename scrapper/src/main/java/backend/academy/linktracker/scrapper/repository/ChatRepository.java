package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Chat;
import java.util.Optional;

public interface ChatRepository {

    boolean registerChat(Chat chat);

    boolean deleteChat(Chat chat);

    Optional<Chat> findById(long chatId);
}
