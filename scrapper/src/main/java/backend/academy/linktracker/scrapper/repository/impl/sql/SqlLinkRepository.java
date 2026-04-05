package backend.academy.linktracker.scrapper.repository.impl.sql;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
public class SqlLinkRepository implements LinkRepository {

    private final JdbcClient jdbcClient;

    @Override
    @Transactional(readOnly = true)
    public List<Link> findLinksForUpdate(OffsetDateTime before, long lastSeenId, int limit) {
        return jdbcClient
                .sql("""
                    select id, uri, tracked_resource, last_update, next_check_at
                    from links
                    where (next_check_at is null or next_check_at <= :before)
                      and id > :lastSeenId
                    order by id
                    limit :limit
                    """)
                .param("before", before)
                .param("lastSeenId", lastSeenId)
                .param("limit", limit)
                .query(this::mapRow)
                .list();
    }

    @Override
    public void addLink(Link link) {
        findByUriAndTrackedResource(link.getUri(), link.getTrackedResource())
                .map(Link::getId)
                .ifPresent(link::setId);

        if (link.getId() != null) {
            return;
        }

        Long id = jdbcClient
                .sql("""
                insert into links(uri, tracked_resource, last_update, next_check_at)
                values (:uri, :trackedResource, :lastUpdate, :nextCheckAt)
                returning id
                """)
                .param("uri", link.getUri().toString())
                .param("trackedResource", link.getTrackedResource().name())
                .param("lastUpdate", link.getLastUpdate())
                .param("nextCheckAt", link.getNextCheckAt())
                .query(Long.class)
                .single();

        link.setId(id);
    }

    @Override
    public void updateCheckState(long linkId, Instant lastUpdate, OffsetDateTime nextCheckAt) {
        jdbcClient
                .sql("""
                update links
                set last_update = :lastUpdate,
                    next_check_at = :nextCheckAt
                where id = :linkId
                """)
                .param("lastUpdate", lastUpdate == null ? null : Timestamp.from(lastUpdate)) // ← фикс
                .param("nextCheckAt", nextCheckAt)
                .param("linkId", linkId)
                .update();
    }

    @Override
    public void updateNextCheckAt(long linkId, OffsetDateTime nextCheckAt) {
        jdbcClient
                .sql("""
                update links
                set next_check_at = :nextCheckAt
                where id = :linkId
                """)
                .param("nextCheckAt", nextCheckAt)
                .param("linkId", linkId)
                .update();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Link> findById(long id) {
        return jdbcClient.sql("""
                    select id, uri, tracked_resource, last_update, next_check_at
                    from links
                    where id = :id
                    """).param("id", id).query(this::mapRow).optional();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Link> findByUriAndTrackedResource(URI uri, TrackedResource trackedResource) {
        return jdbcClient
                .sql("""
                    select id, uri, tracked_resource, last_update, next_check_at
                    from links
                    where uri = :uri and tracked_resource = :trackedResource
                    """)
                .param("uri", uri.toString())
                .param("trackedResource", trackedResource.name())
                .query(this::mapRow)
                .optional();
    }

    @Override
    public boolean deleteById(long id) {
        int updated = jdbcClient.sql("""
                    delete from links
                    where id = :id
                    """).param("id", id).update();

        return updated > 0;
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private Link mapRow(ResultSet rs, int rowNum) throws SQLException {
        Link link =
                new Link(URI.create(rs.getString("uri")), TrackedResource.valueOf(rs.getString("tracked_resource")));
        link.setId(rs.getLong("id"));
        link.setLastUpdate(
                rs.getTimestamp("last_update") == null
                        ? null
                        : rs.getTimestamp("last_update").toInstant());
        link.setNextCheckAt(rs.getObject("next_check_at", OffsetDateTime.class));
        return link;
    }
}
