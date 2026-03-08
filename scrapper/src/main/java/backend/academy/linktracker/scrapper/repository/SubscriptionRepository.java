package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.common.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.common.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class SubscriptionRepository {

    private final Set<Subscription> subscriptions = ConcurrentHashMap.newKeySet();

    public void addSubscription(Subscription subscription) {

        boolean added = subscriptions.add(subscription);

        if (!added) {
            throw new LinkAlreadyTrackedException();
        }
    }

    public void deleteSubscription(Subscription subscription) {
        boolean deleted = subscriptions.remove(subscription);

        if (!deleted) {
            throw new LinkNotFoundException();
        }
    }

    public List<Subscription> getAllSubscriptionsByChat(Chat chat) {
        return subscriptions.stream()
                .filter(subscription -> subscription.getChat().equals(chat))
                .toList();
    }

    public List<Chat> getAllChatsByLink(Link link) {
        return subscriptions.stream()
                .filter(subscription -> subscription.getLink().equals(link))
                .map(Subscription::getChat)
                .toList();
    }
}
