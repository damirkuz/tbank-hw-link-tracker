package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.jpa.mapper.ChatEntityMapper;
import backend.academy.linktracker.scrapper.repository.jpa.repository.ChatJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
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
        try {
            chatJpaRepository.saveAndFlush(chatEntityMapper.toEntity(chat));
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    @Override
    public boolean deleteChat(Chat chat) {
        return chatJpaRepository.deleteByChatId(chat.getChatId()) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Chat> findById(long chatId) {
        return chatJpaRepository.findById(chatId)
            .map(chatEntityMapper::toDomain);
    }
}
