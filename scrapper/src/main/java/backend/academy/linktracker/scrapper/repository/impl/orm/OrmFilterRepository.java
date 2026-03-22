package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Filter;
import backend.academy.linktracker.scrapper.repository.FilterRepository;
import backend.academy.linktracker.scrapper.repository.jpa.entity.FilterEntity;
import backend.academy.linktracker.scrapper.repository.jpa.entity.SubscriptionEntity;
import backend.academy.linktracker.scrapper.repository.jpa.entity.SubscriptionFilterLinkEntity;
import backend.academy.linktracker.scrapper.repository.jpa.repository.ChatJpaRepository;
import backend.academy.linktracker.scrapper.repository.jpa.repository.FilterJpaRepository;
import backend.academy.linktracker.scrapper.repository.jpa.repository.SubscriptionFilterLinkJpaRepository;
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
public class OrmFilterRepository implements FilterRepository {

    private final FilterJpaRepository filterJpaRepository;
    private final ChatJpaRepository chatJpaRepository;
    private final SubscriptionJpaRepository subscriptionJpaRepository;
    private final SubscriptionFilterLinkJpaRepository subscriptionFilterLinkJpaRepository;

    @Override
    public boolean addFilter(Filter filter) {
        return chatJpaRepository
                .findById(filter.getChat().getChatId())
                .map(chat -> {
                    try {
                        FilterEntity entity = new FilterEntity();
                        entity.setChat(chat);
                        entity.setValue(filter.getValue());
                        FilterEntity saved = filterJpaRepository.saveAndFlush(entity);
                        filter.setId(saved.getId());
                        return true;
                    } catch (DataIntegrityViolationException e) {
                        return false;
                    }
                })
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Filter> findById(long id) {
        return filterJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Filter> findByChatIdAndValue(long chatId, String value) {
        return filterJpaRepository.findByChat_ChatIdAndValue(chatId, value).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Filter> findAllByChat(Chat chat) {
        return filterJpaRepository.findAllByChat_ChatIdOrderById(chat.getChatId()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Filter> findAllBySubscription(long subscriptionId) {
        return filterJpaRepository.findAllBySubscriptionId(subscriptionId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean updateValue(long filterId, String newValue) {
        return filterJpaRepository
                .findById(filterId)
                .map(entity -> {
                    entity.setValue(newValue);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public boolean deleteById(long id) {
        if (!filterJpaRepository.existsById(id)) {
            return false;
        }

        filterJpaRepository.deleteById(id);
        filterJpaRepository.flush();
        return true;
    }

    @Override
    public boolean bindToSubscription(long subscriptionId, long filterId) {
        if (subscriptionFilterLinkJpaRepository.existsBySubscription_IdAndFilter_Id(subscriptionId, filterId)) {
            return false;
        }

        Optional<SubscriptionEntity> subscriptionOptional = subscriptionJpaRepository.findById(subscriptionId);
        if (subscriptionOptional.isEmpty()) {
            return false;
        }

        Optional<FilterEntity> filterOptional = filterJpaRepository.findById(filterId);
        if (filterOptional.isEmpty()) {
            return false;
        }

        SubscriptionFilterLinkEntity entity = new SubscriptionFilterLinkEntity();
        entity.setSubscription(subscriptionOptional.orElseThrow());
        entity.setFilter(filterOptional.orElseThrow());
        subscriptionFilterLinkJpaRepository.saveAndFlush(entity);
        return true;
    }

    @Override
    public boolean unbindFromSubscription(long subscriptionId, long filterId) {
        return subscriptionFilterLinkJpaRepository.deleteBySubscription_IdAndFilter_Id(subscriptionId, filterId) > 0;
    }

    private Filter toDomain(FilterEntity entity) {
        Filter filter = new Filter(new Chat(entity.getChat().getChatId()), entity.getValue());
        filter.setId(entity.getId());
        return filter;
    }
}
