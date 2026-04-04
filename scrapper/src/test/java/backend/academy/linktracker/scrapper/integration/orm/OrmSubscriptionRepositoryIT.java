package backend.academy.linktracker.scrapper.integration.orm;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.integration.AbstractIntegrationTest;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmChatRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmSubscriptionRepository;
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

@TestPropertySource(properties = "app.access-type=ORM")
@Transactional
@DisplayName("[ORM] SubscriptionRepository")
class OrmSubscriptionRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private OrmSubscriptionRepository subscriptionRepository;

    @Autowired
    private OrmChatRepository chatRepository;

    @Autowired
    private OrmLinkRepository linkRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final long CHAT_ID = 601L;
    private static final URI TEST_URI = URI.create("https://github.com/orm-sub/repo");
    private static final TrackedResource RESOURCE = TrackedResource.GITHUB;

    private Chat chat;
    private Link link;

    @BeforeEach
    void setUp() {
        chat = new Chat(CHAT_ID);
        chatRepository.registerChat(chat);

        link = new Link(TEST_URI, RESOURCE);
        linkRepository.addLink(link);
    }

    @Test
    @DisplayName("ORM: addSubscription — добавление ссылки: сохранена в БД")
    void addSubscriptionSavesToDb() {
        Subscription subscription = new Subscription(chat, link);
        boolean added = subscriptionRepository.addSubscription(subscription);

        assertThat(added).isTrue();
        assertThat(subscription.getId()).isNotNull();
        assertThat(countSubscriptions()).isEqualTo(1);
    }

    @Test
    @DisplayName("ORM: addSubscription — добавление дублирующей ссылки: ожидается ошибка (false)")
    void addSubscriptionDuplicateReturnsFalse() {
        subscriptionRepository.addSubscription(new Subscription(chat, link));
        boolean duplicate = subscriptionRepository.addSubscription(new Subscription(chat, link));

        assertThat(duplicate).isFalse();
        assertThat(countSubscriptions()).isEqualTo(1);
    }

    @Test
    @DisplayName("ORM: deleteSubscription — удаление ссылки: ссылка отсутствует в БД")
    void deleteSubscriptionRemovesFromDb() {
        Subscription subscription = new Subscription(chat, link);
        subscriptionRepository.addSubscription(subscription);

        subscriptionRepository.deleteSubscription(subscription);

        assertThat(countSubscriptions()).isZero();
    }

    @Test
    @DisplayName("ORM: getAllSubscriptionsByChat — возвращает подписки чата")
    void getAllSubscriptionsByChatReturnsCorrectList() {
        subscriptionRepository.addSubscription(new Subscription(chat, link));

        List<Subscription> result = subscriptionRepository.getAllSubscriptionsByChat(chat);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getLink().getUri()).isEqualTo(TEST_URI);
    }

    @Test
    @DisplayName("ORM: getAllChatsByLink — возвращает чаты, подписанные на ссылку")
    void getAllChatsByLinkReturnsChats() {
        subscriptionRepository.addSubscription(new Subscription(chat, link));

        List<Chat> chats = subscriptionRepository.getAllChatsByLink(link);

        assertThat(chats).extracting(Chat::getId).contains(CHAT_ID);
    }

    @Test
    @DisplayName("ORM: findByChatIdAndLinkId — возвращает подписку если существует")
    void findByChatIdAndLinkIdReturnsSubscription() {
        Subscription subscription = new Subscription(chat, link);
        subscriptionRepository.addSubscription(subscription);

        Optional<Subscription> found = subscriptionRepository.findByChatIdAndLinkId(CHAT_ID, link.getId());

        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("ORM: findByChatIdAndLinkId — empty если подписки нет")
    void findByChatIdAndLinkIdReturnsEmptyIfAbsent() {
        assertThat(subscriptionRepository.findByChatIdAndLinkId(CHAT_ID, link.getId()))
                .isEmpty();
    }

    private int countSubscriptions() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM subscriptions WHERE chat_id = ? AND link_id = ?",
                Integer.class,
                CHAT_ID,
                link.getId());
    }
}
