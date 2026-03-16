package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.contracts.dto.request.AddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.util.TrackedResourceResolver;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotLinkService {

    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final SubscriptionRepository subscriptionRepository;

    private final TrackedResourceResolver trackedResourceResolver;

    public void addLink(long chatId, AddLinkRequest addLinkRequest) {
        Chat chat = chatRepository.getChat(chatId);

        Link link = createLink(addLinkRequest.uri());
        linkRepository.addLink(link);

        Subscription subscription = new Subscription(chat, link, addLinkRequest.tags());
        subscriptionRepository.addSubscription(subscription);
    }

    private Link createLink(String uri) {
        TrackedResource trackedResource = trackedResourceResolver.resolve(URI.create(uri));
        return new Link(uri, trackedResource);
    }

    public void deleteLink(long chatId, RemoveLinkRequest removeLinkRequest) {
        Chat chat = chatRepository.getChat(chatId);
        Link link = createLink(removeLinkRequest.uri());

        Subscription subscription = new Subscription(chat, link, null);
        subscriptionRepository.deleteSubscription(subscription);
    }

    public List<Subscription> getSubscriptionsByChatId(long chatId) {
        Chat chat = chatRepository.getChat(chatId);
        return subscriptionRepository.getAllSubscriptionsByChat(chat);
    }
}
