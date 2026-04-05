package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.link.TrackedResourceResolver;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LinkSubscriptionService {

    private final LinkRepository linkRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TrackedResourceResolver trackedResourceResolver;

    @Transactional(readOnly = true)
    public Optional<Subscription> findSubscription(Chat chat, URI uri) {
        TrackedResource trackedResource = trackedResourceResolver.resolve(uri);

        return linkRepository.findByUriAndTrackedResource(uri, trackedResource).flatMap(link -> {
            if (link.getId() == null) {
                return Optional.empty();
            }
            return subscriptionRepository.findByChatIdAndLinkId(chat.getId(), link.getId());
        });
    }

    public Link getOrCreateLink(URI uri) {
        TrackedResource trackedResource = trackedResourceResolver.resolve(uri);

        Optional<Link> existing = linkRepository.findByUriAndTrackedResource(uri, trackedResource);
        if (existing.isPresent()) {
            return existing.orElseThrow();
        }

        Link link = new Link(uri, trackedResource);
        linkRepository.addLink(link);

        if (link.getId() != null) {
            return link;
        }

        return linkRepository.findByUriAndTrackedResource(uri, trackedResource).orElse(link);
    }

    public void deleteLinkIfOrphan(Link link) {
        if (link.getId() == null) {
            return;
        }

        List<Chat> chats = subscriptionRepository.getAllChatsByLink(link);
        if (chats.isEmpty()) {
            linkRepository.deleteById(link.getId());
        }
    }
}
