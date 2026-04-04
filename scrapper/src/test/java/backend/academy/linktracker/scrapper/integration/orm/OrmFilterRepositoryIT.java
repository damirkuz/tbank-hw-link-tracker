package backend.academy.linktracker.scrapper.integration.orm;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.integration.AbstractIntegrationTest;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Filter;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmChatRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmFilterRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmSubscriptionRepository;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@TestPropertySource(properties = "app.access-type=ORM")
@Transactional
@DisplayName("[ORM] FilterRepository")
class OrmFilterRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private OrmFilterRepository filterRepository;

    @Autowired
    private OrmChatRepository chatRepository;

    @Autowired
    private OrmLinkRepository linkRepository;

    @Autowired
    private OrmSubscriptionRepository subscriptionRepository;

    private static final long CHAT_ID = 801L;
    private static final URI TEST_URI = URI.create("https://github.com/orm-filter/repo");
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
    @DisplayName("ORM: addFilter — фильтр сохранён в БД, id установлен")
    void addFilterSavesAndSetsId() {
        Filter filter = new Filter(chat, "orm-status:open");
        boolean added = filterRepository.addFilter(filter);

        assertThat(added).isTrue();
        assertThat(filter.getId()).isNotNull().isPositive();
    }

    @Test
    @DisplayName("ORM: addFilter — дублирующий фильтр возвращает false")
    void addFilterDuplicateReturnsFalse() {
        filterRepository.addFilter(new Filter(chat, "orm-dup"));
        boolean duplicate = filterRepository.addFilter(new Filter(chat, "orm-dup"));

        assertThat(duplicate).isFalse();
    }

    @Test
    @DisplayName("ORM: findByChatIdAndValue — возвращает фильтр если существует")
    void findByChatIdAndValueReturnsFilter() {
        Filter filter = new Filter(chat, "orm-author:me");
        filterRepository.addFilter(filter);

        Optional<Filter> found = filterRepository.findByChatIdAndValue(CHAT_ID, "orm-author:me");

        assertThat(found).isPresent();
        assertThat(found.get().getValue()).isEqualTo("orm-author:me");
    }

    @Test
    @DisplayName("ORM: updateValue — обновление значения: новое значение сохранено")
    void updateValueChangesFilterValue() {
        Filter filter = new Filter(chat, "orm-old-val");
        filterRepository.addFilter(filter);

        boolean updated = filterRepository.updateValue(filter.getId(), "orm-new-val");

        assertThat(updated).isTrue();
        assertThat(filterRepository.findById(filter.getId()))
            .isPresent()
            .get()
            .extracting(Filter::getValue)
            .isEqualTo("orm-new-val");
    }

    @Test
    @DisplayName("ORM: bindToSubscription / findAllBySubscription — фильтр привязан и возвращается")
    void bindAndFindBySubscription() {
        Filter filter = new Filter(chat, "orm-assignee:me");
        filterRepository.addFilter(filter);

        filterRepository.bindToSubscription(subscription.getId(), filter.getId());
        List<Filter> filters = filterRepository.findAllBySubscription(subscription.getId());

        assertThat(filters).extracting(Filter::getValue).containsExactly("orm-assignee:me");
    }

    @Test
    @DisplayName("ORM: unbindFromSubscription — фильтр отвязан от подписки")
    void unbindRemovesFilterFromSubscription() {
        Filter filter = new Filter(chat, "orm-type:pr");
        filterRepository.addFilter(filter);
        filterRepository.bindToSubscription(subscription.getId(), filter.getId());

        filterRepository.unbindFromSubscription(subscription.getId(), filter.getId());

        assertThat(filterRepository.findAllBySubscription(subscription.getId())).isEmpty();
    }

    @Test
    @DisplayName("ORM: deleteById — фильтр удалён из БД")
    void deleteByIdRemovesFilter() {
        Filter filter = new Filter(chat, "orm-to-delete");
        filterRepository.addFilter(filter);

        boolean deleted = filterRepository.deleteById(filter.getId());

        assertThat(deleted).isTrue();
        assertThat(filterRepository.findById(filter.getId())).isEmpty();
    }
}
