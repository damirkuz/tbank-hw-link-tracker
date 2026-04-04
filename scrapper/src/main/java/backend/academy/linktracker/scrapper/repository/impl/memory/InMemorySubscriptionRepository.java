package backend.academy.linktracker.scrapper.repository.impl.memory;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "MEMORY")
public class InMemorySubscriptionRepository implements SubscriptionRepository {

    private final Set<Subscription> subscriptions = ConcurrentHashMap.newKeySet();

    @Override
    public boolean addSubscription(Subscription subscription) {
        return subscriptions.add(subscription);
    }

    @Override
    public void deleteSubscription(Subscription subscription) {
        subscriptions.remove(subscription);
    }

    @Override
    public List<Subscription> getAllSubscriptionsByChat(Chat chat) {
        return subscriptions.stream()
                .filter(subscription -> subscription.getChat().equals(chat))
                .toList();
    }

    @Override
    public List<Chat> getAllChatsByLink(Link link) {
        return subscriptions.stream()
                .filter(subscription -> subscription.getLink().equals(link))
                .map(Subscription::getChat)
                .distinct()
                .toList();
    }

    @Override
    public Optional<Subscription> findByChatIdAndLinkId(long chatId, long linkId) {
        return subscriptions.stream()
                .filter(subscription -> subscription.getChat().getId() == chatId)
                .filter(subscription -> subscription.getLink().getId() != null)
                .filter(subscription -> subscription.getLink().getId() == linkId)
                .findFirst();
    }
}
