package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.jpa.entity.ChatEntity;
import backend.academy.linktracker.scrapper.repository.jpa.entity.LinkEntity;
import backend.academy.linktracker.scrapper.repository.jpa.mapper.ChatEntityMapper;
import backend.academy.linktracker.scrapper.repository.jpa.mapper.SubscriptionEntityMapper;
import backend.academy.linktracker.scrapper.repository.jpa.repository.ChatJpaRepository;
import backend.academy.linktracker.scrapper.repository.jpa.repository.LinkJpaRepository;
import backend.academy.linktracker.scrapper.repository.jpa.repository.SubscriptionJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "ORM")
public class OrmSubscriptionRepository implements SubscriptionRepository {

    private final ChatJpaRepository chatJpaRepository;
    private final LinkJpaRepository linkJpaRepository;
    private final SubscriptionJpaRepository subscriptionJpaRepository;

    private final ChatEntityMapper chatEntityMapper;
    private final SubscriptionEntityMapper subscriptionEntityMapper;

    @Override
    public boolean addSubscription(Subscription subscription) {
        Optional<ChatEntity> chatEntity =
                chatJpaRepository.findById(subscription.getChat().getChatId());
        if (chatEntity.isEmpty()) {
            return false;
        }

        LinkEntity linkEntity = resolveOrCreateLink(subscription.getLink());

        try {
            subscriptionJpaRepository.saveAndFlush(
                    subscriptionEntityMapper.toEntity(subscription, chatEntity.get(), linkEntity));
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    @Override
    public void deleteSubscription(Subscription subscription) {
        resolveExistingLink(subscription.getLink())
                .ifPresent(linkEntity -> subscriptionJpaRepository.deleteByChat_ChatIdAndLink_Id(
                        subscription.getChat().getChatId(), linkEntity.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> getAllSubscriptionsByChat(Chat chat) {
        return subscriptionJpaRepository.findAllByChat_ChatId(chat.getChatId()).stream()
                .map(subscriptionEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chat> getAllChatsByLink(Link link) {
        return resolveExistingLink(link)
                .map(linkEntity -> subscriptionJpaRepository.findAllByLink_Id(linkEntity.getId()).stream()
                        .map(subscription -> chatEntityMapper.toDomain(subscription.getChat()))
                        .distinct()
                        .toList())
                .orElseGet(List::of);
    }

    private Optional<LinkEntity> resolveExistingLink(Link link) {
        if (link.getId() != null) {
            Optional<LinkEntity> byId = linkJpaRepository.findById(link.getId());
            if (byId.isPresent()) {
                return byId;
            }
        }

        return linkJpaRepository.findByUriAndTrackedResource(link.getUri(), link.getTrackedResource());
    }

    private LinkEntity resolveOrCreateLink(Link link) {
        return resolveExistingLink(link).orElseGet(() -> insertOrLoadExisting(link));
    }

    private LinkEntity insertOrLoadExisting(Link link) {
        try {
            return linkJpaRepository.saveAndFlush(newLinkEntity(link));
        } catch (DataIntegrityViolationException e) {
            return linkJpaRepository
                    .findByUriAndTrackedResource(link.getUri(), link.getTrackedResource())
                    .orElseThrow(() -> e);
        }
    }

    private LinkEntity newLinkEntity(Link link) {
        LinkEntity entity = new LinkEntity();
        entity.setUri(link.getUri());
        entity.setTrackedResource(link.getTrackedResource());
        entity.setLastUpdate(link.getLastUpdate());
        return entity;
    }
}
