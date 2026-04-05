package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.contracts.dto.response.CommonLinkResponse;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Filter;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.FilterRepository;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionMetadataService {

    private final TagRepository tagRepository;
    private final FilterRepository filterRepository;

    public void bindMetadata(Subscription subscription, List<String> rawTags, List<String> rawFilters) {
        bindTags(subscription, normalize(rawTags));
        bindFilters(subscription, normalize(rawFilters));
    }

    public CommonLinkResponse toResponse(Subscription subscription) {
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
        return tagRepository.findByChatIdAndName(chat.getId(), tagName).orElseGet(() -> createOrLoadTag(chat, tagName));
    }

    private Tag createOrLoadTag(Chat chat, String tagName) {
        Tag tag = new Tag(chat, tagName);

        if (tagRepository.addTag(tag)) {
            return tag;
        }

        return tagRepository
                .findByChatIdAndName(chat.getId(), tagName)
                .orElseThrow(() -> new IllegalStateException("Cannot resolve tag after insert attempt"));
    }

    private Filter getOrCreateFilter(Chat chat, String filterValue) {
        return filterRepository
                .findByChatIdAndValue(chat.getId(), filterValue)
                .orElseGet(() -> createOrLoadFilter(chat, filterValue));
    }

    private Filter createOrLoadFilter(Chat chat, String filterValue) {
        Filter filter = new Filter(chat, filterValue);

        if (filterRepository.addFilter(filter)) {
            return filter;
        }

        return filterRepository
                .findByChatIdAndValue(chat.getId(), filterValue)
                .orElseThrow(() -> new IllegalStateException("Cannot resolve filter after insert attempt"));
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
