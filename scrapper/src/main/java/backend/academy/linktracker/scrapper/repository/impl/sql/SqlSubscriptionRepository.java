package backend.academy.linktracker.scrapper.repository.impl.sql;


import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.sql.mapper.ChatSqlMapper;
import backend.academy.linktracker.scrapper.repository.sql.mapper.SubscriptionSqlMapper;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
public class SqlSubscriptionRepository implements SubscriptionRepository {

    private static final String INSERT_SUBSCRIPTION = """
        with existing_chat as (
            select chat_id
            from chats
            where chat_id = ?
        ),
        inserted_link as (
            insert into links (uri, tracked_resource, last_update)
            select ?, ?, ?
            where exists (select 1 from existing_chat)
            on conflict (uri, tracked_resource) do nothing
            returning id
        ),
        resolved_link as (
            select id from inserted_link
            union all
            select l.id
            from links l
            where exists (select 1 from existing_chat)
              and l.uri = ?
              and l.tracked_resource = ?
            limit 1
        ),
        inserted_subscription as (
            insert into subscriptions (chat_id, link_id)
            select ?, rl.id
            from resolved_link rl
            on conflict (chat_id, link_id) do nothing
            returning id
        )
        select id
        from inserted_subscription
        """;

    private static final String DELETE_SUBSCRIPTION = """
        delete from subscriptions
        where chat_id = ? and link_id = ?
        """;

    private static final String FIND_SUBSCRIPTIONS_BY_CHAT = """
        select
            s.id as subscription_id,
            c.chat_id as chat_id,
            l.id as link_id,
            l.uri as uri,
            l.tracked_resource as tracked_resource,
            l.last_update as last_update,
            array_remove(array_agg(distinct st.tag), null) as tags,
            array_remove(array_agg(distinct sf.filter_value), null) as filters
        from subscriptions s
        join chats c on c.chat_id = s.chat_id
        join links l on l.id = s.link_id
        left join subscription_tags st on st.subscription_id = s.id
        left join subscription_filters sf on sf.subscription_id = s.id
        where s.chat_id = ?
        group by s.id, c.chat_id, l.id, l.uri, l.tracked_resource, l.last_update
        order by s.id
        """;

    private static final String FIND_CHATS_BY_LINK_ID = """
        select distinct c.chat_id
        from subscriptions s
        join chats c on c.chat_id = s.chat_id
        where s.link_id = ?
        order by c.chat_id
        """;

    private static final String FIND_LINK_ID_BY_ID = """
        select id
        from links
        where id = ?
        """;

    private static final String FIND_LINK_ID_BY_URI_AND_RESOURCE = """
        select id
        from links
        where uri = ? and tracked_resource = ?
        """;

    private static final String INSERT_TAG = """
        insert into subscription_tags (subscription_id, tag)
        values (?, ?)
        on conflict (subscription_id, tag) do nothing
        """;

    private static final String INSERT_FILTER = """
        insert into subscription_filters (subscription_id, filter_value)
        values (?, ?)
        on conflict (subscription_id, filter_value) do nothing
        """;

    private final JdbcTemplate jdbcTemplate;
    private final SubscriptionSqlMapper subscriptionSqlMapper;
    private final ChatSqlMapper chatSqlMapper;

    @Override
    public boolean addSubscription(Subscription subscription) {
        List<Long> insertedIds = jdbcTemplate.query(
            INSERT_SUBSCRIPTION,
            (rs, rowNum) -> rs.getLong("id"),
            subscription.getChat().getChatId(),
            subscription.getLink().getUri().toString(),
            subscription.getLink().getTrackedResource().name(),
            toTimestamp(subscription.getLink()),
            subscription.getLink().getUri().toString(),
            subscription.getLink().getTrackedResource().name(),
            subscription.getChat().getChatId()
        );

        if (insertedIds.isEmpty()) {
            return false;
        }

        Long subscriptionId = insertedIds.getFirst();
        insertTags(subscriptionId, normalize(subscription.getTags()));
        insertFilters(subscriptionId, normalize(subscription.getFilters()));
        return true;
    }

    @Override
    public void deleteSubscription(Subscription subscription) {
        resolveLinkId(subscription.getLink()).ifPresent(linkId ->
            jdbcTemplate.update(
                DELETE_SUBSCRIPTION,
                subscription.getChat().getChatId(),
                linkId
            )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> getAllSubscriptionsByChat(Chat chat) {
        return jdbcTemplate.query(
            FIND_SUBSCRIPTIONS_BY_CHAT,
            subscriptionSqlMapper,
            chat.getChatId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chat> getAllChatsByLink(Link link) {
        return resolveLinkId(link)
            .map(linkId -> jdbcTemplate.query(FIND_CHATS_BY_LINK_ID, chatSqlMapper, linkId))
            .orElseGet(List::of);
    }

    private Optional<Long> resolveLinkId(Link link) {
        if (link.getId() != null) {
            List<Long> byId = jdbcTemplate.query(
                FIND_LINK_ID_BY_ID,
                (rs, rowNum) -> rs.getLong("id"),
                link.getId()
            );

            if (!byId.isEmpty()) {
                return Optional.of(byId.getFirst());
            }
        }

        List<Long> byNaturalKey = jdbcTemplate.query(
            FIND_LINK_ID_BY_URI_AND_RESOURCE,
            (rs, rowNum) -> rs.getLong("id"),
            link.getUri().toString(),
            link.getTrackedResource().name()
        );

        return byNaturalKey.stream().findFirst();
    }

    private void insertTags(Long subscriptionId, List<String> tags) {
        if (tags.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
            INSERT_TAG,
            tags,
            tags.size(),
            (PreparedStatement ps, String tag) -> {
                ps.setLong(1, subscriptionId);
                ps.setString(2, tag);
            }
        );
    }

    private void insertFilters(Long subscriptionId, List<String> filters) {
        if (filters.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
            INSERT_FILTER,
            filters,
            filters.size(),
            (PreparedStatement ps, String filter) -> {
                ps.setLong(1, subscriptionId);
                ps.setString(2, filter);
            }
        );
    }

    private List<String> normalize(List<String> values) {
        if (values == null) {
            return List.of();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null) {
                normalized.add(value);
            }
        }

        return List.copyOf(normalized);
    }

    private Timestamp toTimestamp(Link link) {
        return link.getLastUpdate() == null ? null : Timestamp.from(link.getLastUpdate());
    }
}
