package backend.academy.linktracker.scrapper.repository.impl.sql;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.sql.mapper.ChatSqlMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
public class SqlChatRepository implements ChatRepository {

    private static final String INSERT_CHAT = """
        insert into chats (id)
        values (?)
        on conflict (id) do nothing
        """;

    private static final String DELETE_CHAT = """
        delete from chats
        where id = ?
        """;

    private static final String FIND_CHAT_BY_ID = """
        select id
        from chats
        where id = ?
        """;

    private final JdbcTemplate jdbcTemplate;
    private final ChatSqlMapper chatSqlMapper;

    @Override
    public boolean registerChat(Chat chat) {
        return jdbcTemplate.update(INSERT_CHAT, chat.getId()) > 0;
    }

    @Override
    public boolean deleteChat(Chat chat) {
        return jdbcTemplate.update(DELETE_CHAT, chat.getId()) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Chat> findById(long chatId) {
        return jdbcTemplate.query(FIND_CHAT_BY_ID, chatSqlMapper, chatId).stream()
                .findFirst();
    }
}
