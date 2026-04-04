package backend.academy.linktracker.scrapper.integration.orm;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.integration.AbstractIntegrationTest;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmLinkRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@TestPropertySource(properties = "app.access-type=ORM")
@Transactional
@DisplayName("[ORM] LinkRepository")
class OrmLinkRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private OrmLinkRepository linkRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final URI TEST_URI = URI.create("https://github.com/orm-link/repo");
    private static final TrackedResource RESOURCE = TrackedResource.GITHUB;

    @Test
    @DisplayName("ORM: addLink — добавление ссылки: ссылка сохранена в БД, id установлен")
    void addLinkSavesAndSetsId() {
        Link link = new Link(TEST_URI, RESOURCE);

        linkRepository.addLink(link);

        assertThat(link.getId()).isNotNull().isPositive();
        assertThat(countLinks()).isEqualTo(1);
    }

    @Test
    @DisplayName("ORM: addLink — добавление дублирующей ссылки: ожидается игнорирование")
    void addLinkDuplicateIsIdempotent() {
        linkRepository.addLink(new Link(TEST_URI, RESOURCE));
        linkRepository.addLink(new Link(TEST_URI, RESOURCE));

        assertThat(countLinks()).isEqualTo(1);
    }

    @Test
    @DisplayName("ORM: findByUriAndTrackedResource — возвращает ссылку")
    void findByUriReturnsLink() {
        Link link = new Link(TEST_URI, RESOURCE);
        linkRepository.addLink(link);

        Optional<Link> found = linkRepository.findByUriAndTrackedResource(TEST_URI, RESOURCE);

        assertThat(found).isPresent();
        assertThat(found.get().getUri()).isEqualTo(TEST_URI);
    }

    @Test
    @DisplayName("ORM: updateCheckState — обновление last_update и next_check_at")
    void updateCheckStateSavesValues() {
        Link link = new Link(TEST_URI, RESOURCE);
        linkRepository.addLink(link);

        OffsetDateTime nextCheck = OffsetDateTime.now().plusHours(2);
        linkRepository.updateCheckState(link.getId(), java.time.Instant.now(), nextCheck);

        Optional<Link> updated = linkRepository.findById(link.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getLastUpdate()).isNotNull();
    }

    @Test
    @DisplayName("ORM: deleteById — удаление ссылки: ссылка отсутствует в БД")
    void deleteByIdRemovesLink() {
        Link link = new Link(TEST_URI, RESOURCE);
        linkRepository.addLink(link);

        boolean deleted = linkRepository.deleteById(link.getId());

        assertThat(deleted).isTrue();
        assertThat(countLinks()).isZero();
    }

    private int countLinks() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM links WHERE uri = ? AND tracked_resource = ?",
                Integer.class,
                TEST_URI.toString(),
                RESOURCE.name());
    }
}
