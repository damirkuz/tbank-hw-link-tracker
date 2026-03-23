package backend.academy.linktracker.scrapper.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@TestPropertySource(properties = "app.access-type=SQL")
@Transactional
@DisplayName("Тест миграций: приложение стартует с чистой БД")
class MigrationIT extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Коннект к БД успешен — миграции применены без ошибок")
    void connectionIsSuccessful() {
        assertThatCode(() -> dataSource.getConnection().close()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Таблица chats создана")
    void chatsTableExists() {
        assertTableExists("chats");
    }

    @Test
    @DisplayName("Таблица links создана")
    void linksTableExists() {
        assertTableExists("links");
    }

    @Test
    @DisplayName("Таблица subscriptions создана")
    void subscriptionsTableExists() {
        assertTableExists("subscriptions");
    }

    @Test
    @DisplayName("Таблица tags создана")
    void tagsTableExists() {
        assertTableExists("tags");
    }

    @Test
    @DisplayName("Таблица filters создана")
    void filtersTableExists() {
        assertTableExists("filters");
    }

    @Test
    @DisplayName("Таблица subscription_tag_links создана")
    void subscriptionTagLinksTableExists() {
        assertTableExists("subscription_tag_links");
    }

    @Test
    @DisplayName("Таблица subscription_filter_links создана")
    void subscriptionFilterLinksTableExists() {
        assertTableExists("subscription_filter_links");
    }

    private void assertTableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " + "WHERE table_schema = 'public' AND table_name = ?",
                Integer.class,
                tableName);
        assertThat(count).as("Таблица %s должна существовать", tableName).isEqualTo(1);
    }
}
