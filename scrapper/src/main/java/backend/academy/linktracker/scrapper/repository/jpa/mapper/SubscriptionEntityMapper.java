package backend.academy.linktracker.scrapper.repository.jpa.mapper;

import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.repository.jpa.entity.ChatEntity;
import backend.academy.linktracker.scrapper.repository.jpa.entity.LinkEntity;
import backend.academy.linktracker.scrapper.repository.jpa.entity.SubscriptionEntity;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionEntityMapper {

    private final ChatEntityMapper chatEntityMapper;
    private final LinkEntityMapper linkEntityMapper;

    public SubscriptionEntityMapper(ChatEntityMapper chatEntityMapper, LinkEntityMapper linkEntityMapper) {
        this.chatEntityMapper = chatEntityMapper;
        this.linkEntityMapper = linkEntityMapper;
    }

    public SubscriptionEntity toEntity(Subscription subscription, ChatEntity chatEntity, LinkEntity linkEntity) {
        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setChat(chatEntity);
        entity.setLink(linkEntity);
        entity.setTags(normalizeToSet(subscription.getTags()));
        entity.setFilters(normalizeToSet(subscription.getFilters()));
        return entity;
    }

    public Subscription toDomain(SubscriptionEntity entity) {
        return new Subscription(
                chatEntityMapper.toDomain(entity.getChat()),
                linkEntityMapper.toDomain(entity.getLink()),
                List.copyOf(entity.getTags()),
                List.copyOf(entity.getFilters()));
    }

    private Set<String> normalizeToSet(List<String> values) {
        if (values == null) {
            return new LinkedHashSet<>();
        }

        return values.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }
}
