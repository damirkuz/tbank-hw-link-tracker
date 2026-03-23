package backend.academy.linktracker.scrapper.integration.sql;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.integration.AbstractIntegrationTest;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.impl.sql.SqlChatRepository;
import backend.academy.linktracker.scrapper.repository.impl.sql.SqlLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.sql.SqlSubscriptionRepository;
import java.net.URI;
import java.util.List;
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
@DisplayName("[SQL] SubscriptionRepository")
class SqlSubscriptionRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private SqlSubscriptionRepository subscriptionRepository;

    @Autowired
    private SqlChatRepository chatRepository;

    @Autowired
    private SqlLinkRepository linkRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final long CHAT_ID = 201L;
    private static final URI TEST_URI = URI.create("https://github.com/sql-sub/repo");
    private static final TrackedResource RESOURCE = TrackedResource.GITHUB;

    private Chat chat;
    private Link link;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM subscriptions WHERE chat_id = ?", CHAT_ID);
        jdbcTemplate.update(
                "DELETE FROM links WHERE uri = ? AND tracked_resource = ?", TEST_URI.toString(), RESOURCE.name());
        jdbcTemplate.update("DELETE FROM chats WHERE chat_id = ?", CHAT_ID);

        chat = new Chat(CHAT_ID);
        chatRepository.registerChat(chat);

        link = new Link(TEST_URI, RESOURCE);
        linkRepository.addLink(link);
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM subscriptions WHERE chat_id = ?", CHAT_ID);
        jdbcTemplate.update(
                "DELETE FROM links WHERE uri = ? AND tracked_resource = ?", TEST_URI.toString(), RESOURCE.name());
        jdbcTemplate.update("DELETE FROM chats WHERE chat_id = ?", CHAT_ID);
    }

    @Test
    @DisplayName("addSubscription — добавление ссылки: ссылка сохранена в БД, id установлен")
    void addSubscriptionSavesToDb() {
        Subscription subscription = new Subscription(chat, link);
        boolean added = subscriptionRepository.addSubscription(subscription);

        assertThat(added).isTrue();
        assertThat(subscription.getId()).isNotNull();
        assertThat(countSubscriptions()).isEqualTo(1);
    }

    @Test
    @DisplayName("addSubscription — добавление дублирующей ссылки: ожидается ошибка (false)")
    void addSubscriptionDuplicateReturnsFalse() {
        subscriptionRepository.addSubscription(new Subscription(chat, link));
        boolean duplicate = subscriptionRepository.addSubscription(new Subscription(chat, link));

        assertThat(duplicate).isFalse();
        assertThat(countSubscriptions()).isEqualTo(1);
    }

    @Test
    @DisplayName("deleteSubscription — удаление ссылки: ссылка отсутствует в БД")
    void deleteSubscriptionRemovesFromDb() {
        Subscription subscription = new Subscription(chat, link);
        subscriptionRepository.addSubscription(subscription);

        subscriptionRepository.deleteSubscription(subscription);

        assertThat(countSubscriptions()).isZero();
    }

    @Test
    @DisplayName("getAllSubscriptionsByChat — возвращает подписки чата")
    void getAllSubscriptionsByChatReturnsCorrectList() {
        subscriptionRepository.addSubscription(new Subscription(chat, link));

        List<Subscription> result = subscriptionRepository.getAllSubscriptionsByChat(chat);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLink().getUri()).isEqualTo(TEST_URI);
    }

    @Test
    @DisplayName("getAllChatsByLink — возвращает чаты, подписанные на ссылку")
    void getAllChatsByLinkReturnsChats() {
        subscriptionRepository.addSubscription(new Subscription(chat, link));

        List<Chat> chats = subscriptionRepository.getAllChatsByLink(link);

        assertThat(chats).extracting(Chat::getChatId).contains(CHAT_ID);
    }

    @Test
    @DisplayName("findByChatIdAndLinkId — возвращает подписку если существует")
    void findByChatIdAndLinkIdReturnsSubscription() {
        Subscription subscription = new Subscription(chat, link);
        subscriptionRepository.addSubscription(subscription);

        Optional<Subscription> found = subscriptionRepository.findByChatIdAndLinkId(CHAT_ID, link.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(subscription.getId());
    }

    @Test
    @DisplayName("findByChatIdAndLinkId — empty если подписки нет")
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
