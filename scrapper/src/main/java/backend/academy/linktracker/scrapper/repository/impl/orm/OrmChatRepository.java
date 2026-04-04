package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.jpa.entity.ChatEntity;
import backend.academy.linktracker.scrapper.repository.jpa.mapper.ChatEntityMapper;
import backend.academy.linktracker.scrapper.repository.jpa.repository.ChatJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "ORM")
public class OrmChatRepository implements ChatRepository {

    private final ChatJpaRepository chatJpaRepository;
    private final ChatEntityMapper chatEntityMapper;

    @Override
    public boolean registerChat(Chat chat) {
        if (chatJpaRepository.existsById(chat.getId())) {
            return false;
        }
        chatJpaRepository.saveAndFlush(new ChatEntity(chat.getId()));
        return true;
    }

    @Override
    public boolean deleteChat(Chat chat) {
        return chatJpaRepository.deleteRowById(chat.getId()) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Chat> findById(long chatId) {
        return chatJpaRepository.findById(chatId).map(chatEntityMapper::toDomain);
    }
}
