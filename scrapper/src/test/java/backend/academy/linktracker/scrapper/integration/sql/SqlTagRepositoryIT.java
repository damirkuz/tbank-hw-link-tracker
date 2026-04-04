package backend.academy.linktracker.scrapper.integration.sql;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.integration.AbstractIntegrationTest;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.impl.sql.SqlChatRepository;
import backend.academy.linktracker.scrapper.repository.impl.sql.SqlLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.sql.SqlSubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.impl.sql.SqlTagRepository;
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
@DisplayName("[SQL] TagRepository")
class SqlTagRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private SqlTagRepository tagRepository;

    @Autowired
    private SqlChatRepository chatRepository;

    @Autowired
    private SqlLinkRepository linkRepository;

    @Autowired
    private SqlSubscriptionRepository subscriptionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final long CHAT_ID = 301L;
    private static final URI TEST_URI = URI.create("https://github.com/sql-tag/repo");
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
    @DisplayName("addTag — тег сохранён в БД, id установлен")
    void addTagSavesTagAndSetsId() {
        Tag tag = new Tag(chat, "java");
        boolean added = tagRepository.addTag(tag);

        assertThat(added).isTrue();
        assertThat(tag.getId()).isNotNull().isPositive();
    }

    @Test
    @DisplayName("addTag — дублирующий тег возвращает false")
    void addTagDuplicateReturnsFalse() {
        tagRepository.addTag(new Tag(chat, "java"));
        boolean duplicate = tagRepository.addTag(new Tag(chat, "java"));

        assertThat(duplicate).isFalse();
    }

    @Test
    @DisplayName("findByChatIdAndName — возвращает тег если существует")
    void findByChatIdAndNameReturnsTag() {
        Tag tag = new Tag(chat, "spring");
        tagRepository.addTag(tag);

        Optional<Tag> found = tagRepository.findByChatIdAndName(CHAT_ID, "spring");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("spring");
    }

    @Test
    @DisplayName("updateName — обновление имени тега: новое имя сохранено в БД")
    void updateNameChangesTagName() {
        Tag tag = new Tag(chat, "old-name");
        tagRepository.addTag(tag);

        boolean updated = tagRepository.updateName(tag.getId(), "new-name");

        assertThat(updated).isTrue();
        assertThat(tagRepository.findById(tag.getId()))
            .isPresent()
            .get()
            .extracting(Tag::getName)
            .isEqualTo("new-name");
    }

    @Test
    @DisplayName("bindToSubscription / findAllBySubscription — тег привязан и возвращается")
    void bindAndFindBySubscription() {
        Tag tag = new Tag(chat, "backend");
        tagRepository.addTag(tag);

        tagRepository.bindToSubscription(subscription.getId(), tag.getId());
        List<Tag> tags = tagRepository.findAllBySubscription(subscription.getId());

        assertThat(tags).extracting(Tag::getName).containsExactly("backend");
    }

    @Test
    @DisplayName("unbindFromSubscription — тег отвязан от подписки")
    void unbindRemovesTagFromSubscription() {
        Tag tag = new Tag(chat, "devops");
        tagRepository.addTag(tag);
        tagRepository.bindToSubscription(subscription.getId(), tag.getId());

        tagRepository.unbindFromSubscription(subscription.getId(), tag.getId());

        assertThat(tagRepository.findAllBySubscription(subscription.getId())).isEmpty();
    }

    @Test
    @DisplayName("deleteById — тег удалён из БД")
    void deleteByIdRemovesTag() {
        Tag tag = new Tag(chat, "to-delete");
        tagRepository.addTag(tag);

        boolean deleted = tagRepository.deleteById(tag.getId());

        assertThat(deleted).isTrue();
        assertThat(tagRepository.findById(tag.getId())).isEmpty();
    }
}
