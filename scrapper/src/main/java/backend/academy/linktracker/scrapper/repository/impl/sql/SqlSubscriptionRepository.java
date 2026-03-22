package backend.academy.linktracker.scrapper.repository.impl.sql;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
public class SqlSubscriptionRepository implements SubscriptionRepository {

    @Override
    public boolean addSubscription(Subscription subscription) {
        return false;
    }

    @Override
    public void deleteSubscription(Subscription subscription) {}

    @Override
    public List<Subscription> getAllSubscriptionsByChat(Chat chat) {
        return List.of();
    }

    @Override
    public List<Chat> getAllChatsByLink(Link link) {
        return List.of();
    }
}
