package backend.academy.linktracker.scrapper.repository.impl.sql;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Filter;
import backend.academy.linktracker.scrapper.repository.FilterRepository;
import backend.academy.linktracker.scrapper.repository.sql.mapper.FilterSqlMapper;
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
public class SqlFilterRepository implements FilterRepository {

    private static final String INSERT_FILTER = """
        insert into filters (chat_id, value)
        values (?, ?)
        on conflict (chat_id, value) do nothing
        returning id
        """;

    private static final String FIND_BY_ID = """
        select id, chat_id, value
        from filters
        where id = ?
        """;

    private static final String FIND_BY_CHAT_AND_VALUE = """
        select id, chat_id, value
        from filters
        where chat_id = ? and value = ?
        """;

    private static final String FIND_ALL_BY_CHAT = """
        select id, chat_id, value
        from filters
        where chat_id = ?
        order by id
        """;

    private static final String FIND_ALL_BY_SUBSCRIPTION = """
        select f.id, f.chat_id, f.value
        from subscription_filter_links sfl
        join filters f on f.id = sfl.filter_id
        where sfl.subscription_id = ?
        order by f.id
        """;

    private static final String UPDATE_VALUE = """
        update filters
        set value = ?
        where id = ?
        """;

    private static final String DELETE_BY_ID = """
        delete from filters
        where id = ?
        """;

    private static final String BIND_TO_SUBSCRIPTION = """
        insert into subscription_filter_links (subscription_id, filter_id)
        values (?, ?)
        on conflict (subscription_id, filter_id) do nothing
        """;

    private static final String UNBIND_FROM_SUBSCRIPTION = """
        delete from subscription_filter_links
        where subscription_id = ? and filter_id = ?
        """;

    private final JdbcTemplate jdbcTemplate;
    private final FilterSqlMapper filterSqlMapper;

    @Override
    public boolean addFilter(Filter filter) {
        List<Long> ids = jdbcTemplate.query(
                INSERT_FILTER,
                (rs, rowNum) -> rs.getLong("id"),
                filter.getChat().getId(),
                filter.getValue());

        if (ids.isEmpty()) {
            return false;
        }

        filter.setId(ids.getFirst());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Filter> findById(long id) {
        return jdbcTemplate.query(FIND_BY_ID, filterSqlMapper, id).stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Filter> findByChatIdAndValue(long chatId, String value) {
        return jdbcTemplate.query(FIND_BY_CHAT_AND_VALUE, filterSqlMapper, chatId, value).stream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Filter> findAllByChat(Chat chat) {
        return jdbcTemplate.query(FIND_ALL_BY_CHAT, filterSqlMapper, chat.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Filter> findAllBySubscription(long subscriptionId) {
        return jdbcTemplate.query(FIND_ALL_BY_SUBSCRIPTION, filterSqlMapper, subscriptionId);
    }

    @Override
    public boolean updateValue(long filterId, String newValue) {
        return jdbcTemplate.update(UPDATE_VALUE, newValue, filterId) > 0;
    }

    @Override
    public boolean deleteById(long id) {
        return jdbcTemplate.update(DELETE_BY_ID, id) > 0;
    }

    @Override
    public boolean bindToSubscription(long subscriptionId, long filterId) {
        return jdbcTemplate.update(BIND_TO_SUBSCRIPTION, subscriptionId, filterId) > 0;
    }

    @Override
    public boolean unbindFromSubscription(long subscriptionId, long filterId) {
        return jdbcTemplate.update(UNBIND_FROM_SUBSCRIPTION, subscriptionId, filterId) > 0;
    }
}
