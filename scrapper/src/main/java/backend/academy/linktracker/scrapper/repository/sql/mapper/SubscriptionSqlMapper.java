package backend.academy.linktracker.scrapper.repository.sql.mapper;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.net.URI;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

        Timestamp lastUpdate = rs.getTimestamp("last_update");
        if (lastUpdate != null) {
            link.setLastUpdate(lastUpdate.toInstant());
        }

        return new Subscription(chat, link, readStringArray(rs, "tags"), readStringArray(rs, "filters"));
    }

    private List<String> readStringArray(ResultSet rs, String column) throws SQLException {
        Array sqlArray = rs.getArray(column);
        if (sqlArray == null) {
            return List.of();
        }

        Object raw = sqlArray.getArray();
        if (raw instanceof String[] values) {
            return Arrays.stream(values).filter(Objects::nonNull).toList();
        }

        Object[] values = (Object[]) raw;
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();
    }
}
