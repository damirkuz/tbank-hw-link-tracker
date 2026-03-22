package backend.academy.linktracker.scrapper.repository.sql.mapper;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class LinkSqlMapper implements RowMapper<Link> {

    @Override
    public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
        Link link =
                new Link(URI.create(rs.getString("uri")), TrackedResource.valueOf(rs.getString("tracked_resource")));

        link.setId(rs.getLong("id"));

        Timestamp lastUpdate = rs.getTimestamp("last_update");
        if (lastUpdate != null) {
            link.setLastUpdate(lastUpdate.toInstant());
        }

        return link;
    }
}
