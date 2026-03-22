package backend.academy.linktracker.scrapper.repository.impl.sql;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.sql.mapper.LinkSqlMapper;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
public class SqlLinkRepository implements LinkRepository {

    private static final String FIND_ALL_LINKS = """
        select id, uri, tracked_resource, last_update
        from links
        order by id
        """;

    private static final String INSERT_LINK = """
        insert into links (uri, tracked_resource, last_update)
        values (?, ?, ?)
        on conflict (uri, tracked_resource) do nothing
        """;

    private final JdbcTemplate jdbcTemplate;
    private final LinkSqlMapper linkSqlMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Link> getLinks() {
        return jdbcTemplate.query(FIND_ALL_LINKS, linkSqlMapper);
    }

    @Override
    public void addLink(Link link) {
        jdbcTemplate.update(
                INSERT_LINK, link.getUri().toString(), link.getTrackedResource().name(), toTimestamp(link));
    }

    private Timestamp toTimestamp(Link link) {
        return link.getLastUpdate() == null ? null : Timestamp.from(link.getLastUpdate());
    }
}
