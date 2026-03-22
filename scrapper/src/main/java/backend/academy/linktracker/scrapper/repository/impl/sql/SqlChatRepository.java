package backend.academy.linktracker.scrapper.repository.impl.sql;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
public class SqlChatRepository implements ChatRepository {

    @Override
    public boolean registerChat(Chat chat) {
        return false;
    }

    @Override
    public boolean deleteChat(Chat chat) {
        return false;
    }

    @Override
    public Optional<Chat> findById(long chatId) {
        return Optional.empty();
    }
}
