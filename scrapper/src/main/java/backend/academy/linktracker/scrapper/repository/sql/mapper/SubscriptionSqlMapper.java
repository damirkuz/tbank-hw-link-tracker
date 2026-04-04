package backend.academy.linktracker.scrapper.repository.sql.mapper;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionSqlMapper implements RowMapper<Subscription> {

    @Override
    public Subscription mapRow(ResultSet rs, int rowNum) throws SQLException {
        Chat chat = new Chat(rs.getLong("chat_id"));

        Link link =
                new Link(URI.create(rs.getString("uri")), TrackedResource.valueOf(rs.getString("tracked_resource")));
        link.setId(rs.getLong("link_id"));
        link.setLastUpdate(
                rs.getTimestamp("last_update") == null
                        ? null
                        : rs.getTimestamp("last_update").toInstant());
        link.setNextCheckAt(rs.getObject("next_check_at", OffsetDateTime.class));

        Subscription subscription = new Subscription(chat, link);
        subscription.setId(rs.getLong("subscription_id"));
        return subscription;
    }
}
