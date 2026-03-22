package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import java.util.List;

public interface SubscriptionRepository {

    boolean addSubscription(Subscription subscription);

    void deleteSubscription(Subscription subscription);

    List<Subscription> getAllSubscriptionsByChat(Chat chat);

    List<Chat> getAllChatsByLink(Link link);
}
