package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonLinkResponse;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.link.TrackedResourceResolver;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Filter;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.FilterRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BotLinkService {

    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TagRepository tagRepository;
    private final FilterRepository filterRepository;

    private final TrackedResourceResolver trackedResourceResolver;

    @Transactional
    public CommonLinkResponse addLink(long chatId, CommonAddLinkRequest addLinkRequest)
            throws ChatNotFoundException, LinkAlreadyTrackedException {
        Chat chat = getChat(chatId);
        Link link = getOrCreateLink(addLinkRequest.uri());

        Subscription subscription = new Subscription(chat, link);
        boolean added = subscriptionRepository.addSubscription(subscription);

        if (!added) {
            throw new LinkAlreadyTrackedException();
        }

        bindTags(subscription, normalize(addLinkRequest.tags()));
        bindFilters(subscription, normalize(addLinkRequest.filters()));

        return toResponse(subscription);
    }

    @Transactional
    public CommonLinkResponse deleteLink(long chatId, CommonRemoveLinkRequest removeLinkRequest)
            throws ChatNotFoundException {
        Chat chat = getChat(chatId);

        Optional<Subscription> subscriptionOptional = findSubscription(chat, removeLinkRequest.uri());
        if (subscriptionOptional.isEmpty()) {
            return new CommonLinkResponse(null, removeLinkRequest.uri(), List.of(), List.of());
        }

        Subscription subscription = subscriptionOptional.orElseThrow();
        CommonLinkResponse response = toResponse(subscription);

        unbindAllTags(subscription);
        unbindAllFilters(subscription);
        subscriptionRepository.deleteSubscription(subscription);
        deleteLinkIfOrphan(subscription.getLink());

        return response;
    }

    @Transactional(readOnly = true)
    public CommonListLinksResponse getLinks(long chatId) throws ChatNotFoundException {
        List<Subscription> subscriptionList = getSubscriptionsByChatId(chatId);
        List<CommonLinkResponse> linkResponses = new ArrayList<>(subscriptionList.size());

        for (Subscription subscription : subscriptionList) {
            linkResponses.add(toResponse(subscription));
        }

        return new CommonListLinksResponse(linkResponses, linkResponses.size());
    }

    @Transactional(readOnly = true)
    public List<Subscription> getSubscriptionsByChatId(long chatId) throws ChatNotFoundException {
        Chat chat = getChat(chatId);
        return subscriptionRepository.getAllSubscriptionsByChat(chat);
    }

    @Transactional(readOnly = true)
    public Chat getChat(long chatId) throws ChatNotFoundException {
        return chatRepository.findById(chatId).orElseThrow(ChatNotFoundException::new);
    }

    private Optional<Subscription> findSubscription(Chat chat, URI uri) {
        TrackedResource trackedResource = trackedResourceResolver.resolve(uri);

        return linkRepository.findByUriAndTrackedResource(uri, trackedResource).flatMap(link -> {
            if (link.getId() == null) {
                return Optional.empty();
            }
            return subscriptionRepository.findByChatIdAndLinkId(chat.getChatId(), link.getId());
        });
    }

    private Link getOrCreateLink(URI uri) {
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

    private void bindTags(Subscription subscription, List<String> tagNames) {
        if (subscription.getId() == null || tagNames.isEmpty()) {
            return;
        }

        for (String tagName : tagNames) {
            Tag tag = getOrCreateTag(subscription.getChat(), tagName);
            if (tag.getId() != null) {
                tagRepository.bindToSubscription(subscription.getId(), tag.getId());
            }
        }
    }

    private void bindFilters(Subscription subscription, List<String> filterValues) {
        if (subscription.getId() == null || filterValues.isEmpty()) {
            return;
        }

        for (String filterValue : filterValues) {
            Filter filter = getOrCreateFilter(subscription.getChat(), filterValue);
            if (filter.getId() != null) {
                filterRepository.bindToSubscription(subscription.getId(), filter.getId());
            }
        }
    }

    private Tag getOrCreateTag(Chat chat, String tagName) {
        return tagRepository
                .findByChatIdAndName(chat.getChatId(), tagName)
                .orElseGet(() -> createOrLoadTag(chat, tagName));
    }

    private Tag createOrLoadTag(Chat chat, String tagName) {
        Tag tag = new Tag(chat, tagName);

        if (tagRepository.addTag(tag)) {
            return tag;
        }

        return tagRepository
                .findByChatIdAndName(chat.getChatId(), tagName)
                .orElseThrow(() -> new IllegalStateException("Cannot resolve tag after insert attempt"));
    }

    private Filter getOrCreateFilter(Chat chat, String filterValue) {
        return filterRepository
                .findByChatIdAndValue(chat.getChatId(), filterValue)
                .orElseGet(() -> createOrLoadFilter(chat, filterValue));
    }

    private Filter createOrLoadFilter(Chat chat, String filterValue) {
        Filter filter = new Filter(chat, filterValue);

        if (filterRepository.addFilter(filter)) {
            return filter;
        }

        return filterRepository
                .findByChatIdAndValue(chat.getChatId(), filterValue)
                .orElseThrow(() -> new IllegalStateException("Cannot resolve filter after insert attempt"));
    }

    private void unbindAllTags(Subscription subscription) {
        if (subscription.getId() == null) {
            return;
        }

        List<Tag> tags = tagRepository.findAllBySubscription(subscription.getId());
        for (Tag tag : tags) {
            if (tag.getId() != null) {
                tagRepository.unbindFromSubscription(subscription.getId(), tag.getId());
            }
        }
    }

    private void unbindAllFilters(Subscription subscription) {
        if (subscription.getId() == null) {
            return;
        }

        List<Filter> filters = filterRepository.findAllBySubscription(subscription.getId());
        for (Filter filter : filters) {
            if (filter.getId() != null) {
                filterRepository.unbindFromSubscription(subscription.getId(), filter.getId());
            }
        }
    }

    private void deleteLinkIfOrphan(Link link) {
        if (link.getId() == null) {
            return;
        }

        List<Chat> chats = subscriptionRepository.getAllChatsByLink(link);
        if (chats.isEmpty()) {
            linkRepository.deleteById(link.getId());
        }
    }

    private CommonLinkResponse toResponse(Subscription subscription) {
        List<String> tags = subscription.getId() == null
                ? List.of()
                : tagRepository.findAllBySubscription(subscription.getId()).stream()
                        .map(Tag::getName)
                        .toList();

        List<String> filters = subscription.getId() == null
                ? List.of()
                : filterRepository.findAllBySubscription(subscription.getId()).stream()
                        .map(Filter::getValue)
                        .toList();

        return new CommonLinkResponse(
                subscription.getLink().getId(), subscription.getLink().getUri(), tags, filters);
    }

    private List<String> normalize(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null) {
                String trimmed = value.trim();
                if (!trimmed.isEmpty()) {
                    normalized.add(trimmed);
                }
            }
        }

        return List.copyOf(normalized);
    }
}
