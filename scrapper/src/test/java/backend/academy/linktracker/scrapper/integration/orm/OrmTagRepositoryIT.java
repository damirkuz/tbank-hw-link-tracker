package backend.academy.linktracker.scrapper.integration.orm;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.integration.AbstractIntegrationTest;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmChatRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmSubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmTagRepository;
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
@DisplayName("[ORM] TagRepository")
class OrmTagRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private OrmTagRepository tagRepository;

    @Autowired
    private OrmChatRepository chatRepository;

    @Autowired
    private OrmLinkRepository linkRepository;

    @Autowired
    private OrmSubscriptionRepository subscriptionRepository;

    private static final long CHAT_ID = 701L;
    private static final URI TEST_URI = URI.create("https://github.com/orm-tag/repo");
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
    @DisplayName("ORM: addTag — тег сохранён в БД, id установлен")
    void addTagSavesAndSetsId() {
        Tag tag = new Tag(chat, "orm-java");
        boolean added = tagRepository.addTag(tag);

        assertThat(added).isTrue();
        assertThat(tag.getId()).isNotNull().isPositive();
    }

    @Test
    @DisplayName("ORM: addTag — дублирующий тег возвращает false")
    void addTagDuplicateReturnsFalse() {
        tagRepository.addTag(new Tag(chat, "dup-tag"));
        boolean duplicate = tagRepository.addTag(new Tag(chat, "dup-tag"));

        assertThat(duplicate).isFalse();
    }

    @Test
    @DisplayName("ORM: findByChatIdAndName — возвращает тег если существует")
    void findByChatIdAndNameReturnsTag() {
        Tag tag = new Tag(chat, "spring-boot");
        tagRepository.addTag(tag);

        Optional<Tag> found = tagRepository.findByChatIdAndName(CHAT_ID, "spring-boot");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("spring-boot");
    }

    @Test
    @DisplayName("ORM: updateName — обновление имени тега: новое имя сохранено")
    void updateNameChangesTagName() {
        Tag tag = new Tag(chat, "orm-old");
        tagRepository.addTag(tag);

        boolean updated = tagRepository.updateName(tag.getId(), "orm-new");

        assertThat(updated).isTrue();
        assertThat(tagRepository.findById(tag.getId()))
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("orm-new");
    }

    @Test
    @DisplayName("ORM: bindToSubscription / findAllBySubscription — тег привязан и возвращается")
    void bindAndFindBySubscription() {
        Tag tag = new Tag(chat, "orm-backend");
        tagRepository.addTag(tag);

        tagRepository.bindToSubscription(subscription.getId(), tag.getId());
        List<Tag> tags = tagRepository.findAllBySubscription(subscription.getId());

        assertThat(tags).extracting(Tag::getName).containsExactly("orm-backend");
    }

    @Test
    @DisplayName("ORM: unbindFromSubscription — тег отвязан от подписки")
    void unbindRemovesTagFromSubscription() {
        Tag tag = new Tag(chat, "orm-devops");
        tagRepository.addTag(tag);
        tagRepository.bindToSubscription(subscription.getId(), tag.getId());

        tagRepository.unbindFromSubscription(subscription.getId(), tag.getId());

        assertThat(tagRepository.findAllBySubscription(subscription.getId())).isEmpty();
    }

    @Test
    @DisplayName("ORM: deleteById — тег удалён из БД")
    void deleteByIdRemovesTag() {
        Tag tag = new Tag(chat, "orm-delete");
        tagRepository.addTag(tag);

        boolean deleted = tagRepository.deleteById(tag.getId());

        assertThat(deleted).isTrue();
        assertThat(tagRepository.findById(tag.getId())).isEmpty();
    }
}
