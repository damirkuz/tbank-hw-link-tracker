package backend.academy.linktracker.scrapper.integration.sql;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.integration.AbstractIntegrationTest;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.impl.sql.SqlLinkRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@TestPropertySource(properties = "app.access-type=SQL")
@Transactional
@DisplayName("[SQL] LinkRepository")
class SqlLinkRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private SqlLinkRepository linkRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final URI TEST_URI = URI.create("https://github.com/sql-test/link-repo");
    private static final TrackedResource RESOURCE = TrackedResource.GITHUB;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        jdbcTemplate.update(
                "DELETE FROM links WHERE uri = ? AND tracked_resource = ?", TEST_URI.toString(), RESOURCE.name());
    }

    @Test
    @DisplayName("addLink — добавление ссылки: ссылка сохранена в БД, id установлен")
    void addLinkSavesLinkAndSetsId() {
        Link link = new Link(TEST_URI, RESOURCE);

        linkRepository.addLink(link);

        assertThat(link.getId()).isNotNull().isPositive();
        assertThat(countLinks()).isEqualTo(1);
    }

    @Test
    @DisplayName("addLink — добавление дублирующей ссылки: ожидается игнорирование, запись одна")
    void addLinkDuplicateIsIdempotent() {
        linkRepository.addLink(new Link(TEST_URI, RESOURCE));
        linkRepository.addLink(new Link(TEST_URI, RESOURCE));

        assertThat(countLinks()).isEqualTo(1);
    }

    @Test
    @DisplayName("findByUriAndTrackedResource — возвращает ссылку если существует")
    void findByUriAndResourceReturnsLink() {
        linkRepository.addLink(new Link(TEST_URI, RESOURCE));

        Optional<Link> found = linkRepository.findByUriAndTrackedResource(TEST_URI, RESOURCE);

        assertThat(found).isPresent();
        assertThat(found.get().getUri()).isEqualTo(TEST_URI);
        assertThat(found.get().getTrackedResource()).isEqualTo(RESOURCE);
    }

    @Test
    @DisplayName("findByUriAndTrackedResource — empty если ссылки нет")
    void findByUriAndResourceReturnsEmpty() {
        assertThat(linkRepository.findByUriAndTrackedResource(TEST_URI, RESOURCE))
                .isEmpty();
    }

    @Test
    @DisplayName("updateCheckState — обновление last_update и next_check_at")
    void updateCheckStateSavesValues() {
        Link link = new Link(TEST_URI, RESOURCE);
        linkRepository.addLink(link);

        OffsetDateTime nextCheck = OffsetDateTime.now().plusHours(1);
        linkRepository.updateCheckState(link.getId(), java.time.Instant.now(), nextCheck);

        Optional<Link> updated = linkRepository.findById(link.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getLastUpdate()).isNotNull();
        assertThat(updated.get().getNextCheckAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteById — удаление ссылки: ссылка отсутствует в БД")
    void deleteByIdRemovesLink() {
        Link link = new Link(TEST_URI, RESOURCE);
        linkRepository.addLink(link);

        boolean deleted = linkRepository.deleteById(link.getId());

        assertThat(deleted).isTrue();
        assertThat(countLinks()).isZero();
    }

    @Test
    @DisplayName("deleteById — удаление несуществующей ссылки: возвращает false")
    void deleteByIdReturnsFalseIfNotExists() {
        assertThat(linkRepository.deleteById(Long.MAX_VALUE)).isFalse();
    }

    private int countLinks() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM links WHERE uri = ? AND tracked_resource = ?",
                Integer.class,
                TEST_URI.toString(),
                RESOURCE.name());
    }
}
