package backend.academy.linktracker.scrapper.repository.impl.sql;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import backend.academy.linktracker.scrapper.repository.sql.mapper.TagSqlMapper;
import java.util.List;
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
public class SqlTagRepository implements TagRepository {

    private static final String INSERT_TAG = """
        insert into tags (chat_id, name)
        values (?, ?)
        on conflict (chat_id, name) do nothing
        returning id
        """;

    private static final String FIND_BY_ID = """
        select id, chat_id, name
        from tags
        where id = ?
        """;

    private static final String FIND_BY_CHAT_AND_NAME = """
        select id, chat_id, name
        from tags
        where chat_id = ? and name = ?
        """;

    private static final String FIND_ALL_BY_CHAT = """
        select id, chat_id, name
        from tags
        where chat_id = ?
        order by id
        """;

    private static final String FIND_ALL_BY_SUBSCRIPTION = """
        select t.id, t.chat_id, t.name
        from subscription_tag_links stl
        join tags t on t.id = stl.tag_id
        where stl.subscription_id = ?
        order by t.id
        """;

    private static final String UPDATE_NAME = """
        update tags
        set name = ?
        where id = ?
        """;

    private static final String DELETE_BY_ID = """
        delete from tags
        where id = ?
        """;

    private static final String BIND_TO_SUBSCRIPTION = """
        insert into subscription_tag_links (subscription_id, tag_id)
        values (?, ?)
        on conflict (subscription_id, tag_id) do nothing
        """;

    private static final String UNBIND_FROM_SUBSCRIPTION = """
        delete from subscription_tag_links
        where subscription_id = ? and tag_id = ?
        """;

    private final JdbcTemplate jdbcTemplate;
    private final TagSqlMapper tagSqlMapper;

    @Override
    public boolean addTag(Tag tag) {
        List<Long> ids = jdbcTemplate.query(
                INSERT_TAG, (rs, rowNum) -> rs.getLong("id"), tag.getChat().getChatId(), tag.getName());

        if (ids.isEmpty()) {
            return false;
        }

        tag.setId(ids.getFirst());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tag> findById(long id) {
        return jdbcTemplate.query(FIND_BY_ID, tagSqlMapper, id).stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tag> findByChatIdAndName(long chatId, String name) {
        return jdbcTemplate.query(FIND_BY_CHAT_AND_NAME, tagSqlMapper, chatId, name).stream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> findAllByChat(Chat chat) {
        return jdbcTemplate.query(FIND_ALL_BY_CHAT, tagSqlMapper, chat.getChatId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> findAllBySubscription(long subscriptionId) {
        return jdbcTemplate.query(FIND_ALL_BY_SUBSCRIPTION, tagSqlMapper, subscriptionId);
    }

    @Override
    public boolean updateName(long tagId, String newName) {
        return jdbcTemplate.update(UPDATE_NAME, newName, tagId) > 0;
    }

    @Override
    public boolean deleteById(long id) {
        return jdbcTemplate.update(DELETE_BY_ID, id) > 0;
    }

    @Override
    public boolean bindToSubscription(long subscriptionId, long tagId) {
        return jdbcTemplate.update(BIND_TO_SUBSCRIPTION, subscriptionId, tagId) > 0;
    }

    @Override
    public boolean unbindFromSubscription(long subscriptionId, long tagId) {
        return jdbcTemplate.update(UNBIND_FROM_SUBSCRIPTION, subscriptionId, tagId) > 0;
    }
}
