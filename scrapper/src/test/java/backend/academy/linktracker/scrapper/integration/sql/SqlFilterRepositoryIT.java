package backend.academy.linktracker.scrapper.integration.sql;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.integration.AbstractIntegrationTest;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Filter;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.impl.sql.SqlChatRepository;
import backend.academy.linktracker.scrapper.repository.impl.sql.SqlFilterRepository;
import backend.academy.linktracker.scrapper.repository.impl.sql.SqlLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.sql.SqlSubscriptionRepository;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@TestPropertySource(properties = "app.access-type=SQL")
@Transactional
@DisplayName("[SQL] FilterRepository")
class SqlFilterRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private SqlFilterRepository filterRepository;

    @Autowired
    private SqlChatRepository chatRepository;

    @Autowired
    private SqlLinkRepository linkRepository;

    @Autowired
    private SqlSubscriptionRepository subscriptionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final long CHAT_ID = 401L;
    private static final URI TEST_URI = URI.create("https://github.com/sql-filter/repo");
    private static final TrackedResource RESOURCE = TrackedResource.GITHUB;

    private Chat chat;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        chat = new Chat(CHAT_ID);
        chatRepository.registerChat(chat);

        Link link = new Link(TEST_URI, RESOURCE);
        linkRepository.addLink(link);

        subscription = new Subscription(chat, link);
        subscriptionRepository.addSubscription(subscription);
    }

    @Test
    @DisplayName("addFilter — фильтр сохранён в БД, id установлен")
    void addFilterSavesFilterAndSetsId() {
        Filter filter = new Filter(chat, "status:open");
        boolean added = filterRepository.addFilter(filter);

        assertThat(added).isTrue();
        assertThat(filter.getId()).isNotNull().isPositive();
    }

    @Test
    @DisplayName("addFilter — дублирующий фильтр возвращает false")
    void addFilterDuplicateReturnsFalse() {
        filterRepository.addFilter(new Filter(chat, "label:bug"));
        boolean duplicate = filterRepository.addFilter(new Filter(chat, "label:bug"));

        assertThat(duplicate).isFalse();
    }

    @Test
    @DisplayName("findByChatIdAndValue — возвращает фильтр если существует")
    void findByChatIdAndValueReturnsFilter() {
        Filter filter = new Filter(chat, "author:me");
        filterRepository.addFilter(filter);

        Optional<Filter> found = filterRepository.findByChatIdAndValue(CHAT_ID, "author:me");

        assertThat(found).isPresent();
        assertThat(found.get().getValue()).isEqualTo("author:me");
    }

    @Test
    @DisplayName("updateValue — обновление значения фильтра: новое значение сохранено в БД")
    void updateValueChangesFilterValue() {
        Filter filter = new Filter(chat, "old-filter");
        filterRepository.addFilter(filter);

        boolean updated = filterRepository.updateValue(filter.getId(), "new-filter");

        assertThat(updated).isTrue();
        assertThat(filterRepository.findById(filter.getId()))
                .isPresent()
                .get()
                .extracting(Filter::getValue)
                .isEqualTo("new-filter");
    }

    @Test
    @DisplayName("bindToSubscription / findAllBySubscription — фильтр привязан и возвращается")
    void bindAndFindBySubscription() {
        Filter filter = new Filter(chat, "assignee:me");
        filterRepository.addFilter(filter);

        filterRepository.bindToSubscription(subscription.getId(), filter.getId());
        List<Filter> filters = filterRepository.findAllBySubscription(subscription.getId());

        assertThat(filters).extracting(Filter::getValue).containsExactly("assignee:me");
    }

    @Test
    @DisplayName("unbindFromSubscription — фильтр отвязан от подписки")
    void unbindRemovesFilterFromSubscription() {
        Filter filter = new Filter(chat, "type:pr");
        filterRepository.addFilter(filter);
        filterRepository.bindToSubscription(subscription.getId(), filter.getId());

        filterRepository.unbindFromSubscription(subscription.getId(), filter.getId());

        assertThat(filterRepository.findAllBySubscription(subscription.getId())).isEmpty();
    }

    @Test
    @DisplayName("deleteById — фильтр удалён из БД")
    void deleteByIdRemovesFilter() {
        Filter filter = new Filter(chat, "to-delete");
        filterRepository.addFilter(filter);

        boolean deleted = filterRepository.deleteById(filter.getId());

        assertThat(deleted).isTrue();
        assertThat(filterRepository.findById(filter.getId())).isEmpty();
    }
}
