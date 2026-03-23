package backend.academy.linktracker.scrapper.integration.sql;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.integration.AbstractIntegrationTest;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.impl.sql.SqlChatRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@TestPropertySource(properties = "app.access-type=SQL")
@Transactional
@DisplayName("[SQL] ChatRepository — переключение access-type: используется SQL-имплементация")
class SqlChatRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private SqlChatRepository chatRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final long CHAT_ID = 101L;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM chats WHERE chat_id = ?", CHAT_ID);
    }

    @Test
    @DisplayName("registerChat — добавление чата: запись сохранена в БД")
    void registerChatSavesToDb() {
        boolean result = chatRepository.registerChat(new Chat(CHAT_ID));

        assertThat(result).isTrue();
        assertThat(countChats(CHAT_ID)).isEqualTo(1);
    }

    @Test
    @DisplayName("registerChat — добавление дублирующего чата: возвращает false, записей по-прежнему 1")
    void registerChatDuplicateReturnsFalse() {
        chatRepository.registerChat(new Chat(CHAT_ID));
        boolean second = chatRepository.registerChat(new Chat(CHAT_ID));

        assertThat(second).isFalse();
        assertThat(countChats(CHAT_ID)).isEqualTo(1);
    }

    @Test
    @DisplayName("findById — возвращает чат если он зарегистрирован")
    void findByIdReturnsExistingChat() {
        chatRepository.registerChat(new Chat(CHAT_ID));

        Optional<Chat> found = chatRepository.findById(CHAT_ID);

        assertThat(found).isPresent();
        assertThat(found.get().getChatId()).isEqualTo(CHAT_ID);
    }

    @Test
    @DisplayName("findById — возвращает empty если чат не зарегистрирован")
    void findByIdReturnsEmptyForMissingChat() {
        assertThat(chatRepository.findById(CHAT_ID)).isEmpty();
    }

    @Test
    @DisplayName("deleteChat — удаление чата: запись отсутствует в БД")
    void deleteChatRemovesFromDb() {
        chatRepository.registerChat(new Chat(CHAT_ID));
        boolean deleted = chatRepository.deleteChat(new Chat(CHAT_ID));

        assertThat(deleted).isTrue();
        assertThat(countChats(CHAT_ID)).isZero();
    }

    @Test
    @DisplayName("deleteChat — удаление несуществующего чата: возвращает false")
    void deleteChatReturnsFalseIfNotExists() {
        assertThat(chatRepository.deleteChat(new Chat(CHAT_ID))).isFalse();
    }

    private int countChats(long chatId) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM chats WHERE chat_id = ?", Integer.class, chatId);
    }
}
