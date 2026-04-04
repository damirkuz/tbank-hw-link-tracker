package backend.academy.linktracker.scrapper.integration.orm;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.integration.AbstractIntegrationTest;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmChatRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@TestPropertySource(properties = "app.access-type=ORM")
@Transactional
@DisplayName("[ORM] ChatRepository — переключение access-type: используется ORM-имплементация")
class OrmChatRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private OrmChatRepository chatRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final long CHAT_ID = 501L;

    @Test
    @DisplayName("ORM: registerChat — добавление чата: запись сохранена в БД")
    void registerChatSavesToDb() {
        boolean result = chatRepository.registerChat(new Chat(CHAT_ID));

        assertThat(result).isTrue();
        assertThat(countChats()).isEqualTo(1);
    }

    @Test
    @DisplayName("ORM: registerChat — добавление дублирующего чата: возвращает false")
    void registerChatDuplicateReturnsFalse() {
        chatRepository.registerChat(new Chat(CHAT_ID));
        boolean second = chatRepository.registerChat(new Chat(CHAT_ID));

        assertThat(second).isFalse();
        assertThat(countChats()).isEqualTo(1);
    }

    @Test
    @DisplayName("ORM: findById — возвращает чат если зарегистрирован")
    void findByIdReturnsExistingChat() {
        chatRepository.registerChat(new Chat(CHAT_ID));

        Optional<Chat> found = chatRepository.findById(CHAT_ID);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(CHAT_ID);
    }

    @Test
    @DisplayName("ORM: deleteChat — удаление чата: запись отсутствует в БД")
    void deleteChatRemovesFromDb() {
        chatRepository.registerChat(new Chat(CHAT_ID));
        boolean deleted = chatRepository.deleteChat(new Chat(CHAT_ID));

        assertThat(deleted).isTrue();
        assertThat(countChats()).isZero();
    }

    private int countChats() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM chats WHERE id = ?",
            Integer.class,
            CHAT_ID
        );
    }
}
