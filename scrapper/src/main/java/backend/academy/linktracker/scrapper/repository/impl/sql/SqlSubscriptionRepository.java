package backend.academy.linktracker.scrapper.repository.impl.sql;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.sql.mapper.ChatSqlMapper;
import backend.academy.linktracker.scrapper.repository.sql.mapper.SubscriptionSqlMapper;
import java.sql.Timestamp;
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
public class SqlSubscriptionRepository implements SubscriptionRepository {

    private static final String INSERT_SUBSCRIPTION = """
        with existing_chat as (
            select chat_id
            from chats
            where chat_id = ?
        ),
        inserted_link as (
            insert into links (uri, tracked_resource, last_update, next_check_at)
            select ?, ?, ?, ?
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
            l.next_check_at as next_check_at
        from subscriptions s
        join chats c on c.chat_id = s.chat_id
        join links l on l.id = s.link_id
        where s.chat_id = ?
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

    private static final String FIND_SUBSCRIPTION_BY_CHAT_AND_LINK = """
        select
            s.id as subscription_id,
            c.chat_id as chat_id,
            l.id as link_id,
            l.uri as uri,
            l.tracked_resource as tracked_resource,
            l.last_update as last_update,
            l.next_check_at as next_check_at
        from subscriptions s
        join chats c on c.chat_id = s.chat_id
        join links l on l.id = s.link_id
        where s.chat_id = ? and s.link_id = ?
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
                subscription.getLink().getNextCheckAt(),
                subscription.getLink().getUri().toString(),
                subscription.getLink().getTrackedResource().name(),
                subscription.getChat().getChatId());

        if (insertedIds.isEmpty()) {
            return false;
        }

        subscription.setId(insertedIds.getFirst());
        return true;
    }

    @Override
    public void deleteSubscription(Subscription subscription) {
        resolveLinkId(subscription.getLink())
                .ifPresent(linkId -> jdbcTemplate.update(
                        DELETE_SUBSCRIPTION, subscription.getChat().getChatId(), linkId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> getAllSubscriptionsByChat(Chat chat) {
        return jdbcTemplate.query(FIND_SUBSCRIPTIONS_BY_CHAT, subscriptionSqlMapper, chat.getChatId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chat> getAllChatsByLink(Link link) {
        return resolveLinkId(link)
                .map(linkId -> jdbcTemplate.query(FIND_CHATS_BY_LINK_ID, chatSqlMapper, linkId))
                .orElseGet(List::of);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findByChatIdAndLinkId(long chatId, long linkId) {
        return jdbcTemplate.query(FIND_SUBSCRIPTION_BY_CHAT_AND_LINK, subscriptionSqlMapper, chatId, linkId).stream()
                .findFirst();
    }

    private Optional<Long> resolveLinkId(Link link) {
        if (link.getId() != null) {
            List<Long> byId = jdbcTemplate.query(FIND_LINK_ID_BY_ID, (rs, rowNum) -> rs.getLong("id"), link.getId());
            if (!byId.isEmpty()) {
                return Optional.of(byId.getFirst());
            }
        }

        List<Long> byNaturalKey = jdbcTemplate.query(
                FIND_LINK_ID_BY_URI_AND_RESOURCE,
                (rs, rowNum) -> rs.getLong("id"),
                link.getUri().toString(),
                link.getTrackedResource().name());

        return byNaturalKey.stream().findFirst();
    }

    private Timestamp toTimestamp(Link link) {
        return link.getLastUpdate() == null ? null : Timestamp.from(link.getLastUpdate());
    }
}
