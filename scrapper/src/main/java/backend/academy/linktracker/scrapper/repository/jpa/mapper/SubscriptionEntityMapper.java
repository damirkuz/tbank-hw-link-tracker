package backend.academy.linktracker.scrapper.repository.jpa.mapper;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.repository.jpa.entity.ChatEntity;
import backend.academy.linktracker.scrapper.repository.jpa.entity.LinkEntity;
import backend.academy.linktracker.scrapper.repository.jpa.entity.SubscriptionEntity;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionEntityMapper {

    public Subscription toDomain(SubscriptionEntity entity) {
        Subscription subscription = new Subscription(
                new backend.academy.linktracker.scrapper.model.Chat(
                        entity.getChat().getId()),
                toLink(entity.getLink()));
        subscription.setId(entity.getId());
        return subscription;
    }

    public SubscriptionEntity toEntity(Subscription subscription, ChatEntity chatEntity, LinkEntity linkEntity) {
        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setChat(chatEntity);
        entity.setLink(linkEntity);
        return entity;
    }

    private Link toLink(LinkEntity entity) {
        Link link = new Link(entity.getUri(), entity.getTrackedResource());
        link.setId(entity.getId());
        link.setLastUpdate(entity.getLastUpdate());
        link.setNextCheckAt(entity.getNextCheckAt());
        return link;
    }
}
