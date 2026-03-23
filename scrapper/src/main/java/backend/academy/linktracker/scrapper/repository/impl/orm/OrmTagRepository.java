package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import backend.academy.linktracker.scrapper.repository.jpa.entity.SubscriptionEntity;
import backend.academy.linktracker.scrapper.repository.jpa.entity.SubscriptionTagLinkEntity;
import backend.academy.linktracker.scrapper.repository.jpa.entity.TagEntity;
import backend.academy.linktracker.scrapper.repository.jpa.repository.ChatJpaRepository;
import backend.academy.linktracker.scrapper.repository.jpa.repository.SubscriptionJpaRepository;
import backend.academy.linktracker.scrapper.repository.jpa.repository.SubscriptionTagLinkJpaRepository;
import backend.academy.linktracker.scrapper.repository.jpa.repository.TagJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "ORM")
public class OrmTagRepository implements TagRepository {

    private final TagJpaRepository tagJpaRepository;
    private final ChatJpaRepository chatJpaRepository;
    private final SubscriptionJpaRepository subscriptionJpaRepository;
    private final SubscriptionTagLinkJpaRepository subscriptionTagLinkJpaRepository;

    @Override
    public boolean addTag(Tag tag) {
        return chatJpaRepository
                .findById(tag.getChat().getChatId())
                .map(chat -> {
                    if (tagJpaRepository.existsByChat_ChatIdAndName(chat.getChatId(), tag.getName())) {
                        return false;
                    }
                    TagEntity entity = new TagEntity();
                    entity.setChat(chat);
                    entity.setName(tag.getName());
                    TagEntity saved = tagJpaRepository.save(entity);
                    tag.setId(saved.getId());
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tag> findById(long id) {
        return tagJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tag> findByChatIdAndName(long chatId, String name) {
        return tagJpaRepository.findByChat_ChatIdAndName(chatId, name).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> findAllByChat(Chat chat) {
        return tagJpaRepository.findAllByChat_ChatIdOrderById(chat.getChatId()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> findAllBySubscription(long subscriptionId) {
        return tagJpaRepository.findAllBySubscriptionId(subscriptionId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean updateName(long tagId, String newName) {
        return tagJpaRepository
                .findById(tagId)
                .map(entity -> {
                    entity.setName(newName);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public boolean deleteById(long id) {
        if (!tagJpaRepository.existsById(id)) {
            return false;
        }

        tagJpaRepository.deleteById(id);
        tagJpaRepository.flush();
        return true;
    }

    @Override
    public boolean bindToSubscription(long subscriptionId, long tagId) {
        if (subscriptionTagLinkJpaRepository.existsBySubscription_IdAndTag_Id(subscriptionId, tagId)) {
            return false;
        }

        Optional<SubscriptionEntity> subscriptionOptional = subscriptionJpaRepository.findById(subscriptionId);
        if (subscriptionOptional.isEmpty()) {
            return false;
        }

        Optional<TagEntity> tagOptional = tagJpaRepository.findById(tagId);
        if (tagOptional.isEmpty()) {
            return false;
        }

        SubscriptionTagLinkEntity entity = new SubscriptionTagLinkEntity();
        entity.setSubscription(subscriptionOptional.orElseThrow());
        entity.setTag(tagOptional.orElseThrow());
        subscriptionTagLinkJpaRepository.saveAndFlush(entity);
        return true;
    }

    @Override
    public boolean unbindFromSubscription(long subscriptionId, long tagId) {
        return subscriptionTagLinkJpaRepository.deleteBySubscription_IdAndTag_Id(subscriptionId, tagId) > 0;
    }

    private Tag toDomain(TagEntity entity) {
        Tag tag = new Tag(new Chat(entity.getChat().getChatId()), entity.getName());
        tag.setId(entity.getId());
        return tag;
    }
}
