package backend.academy.linktracker.scrapper.repository.impl.memory;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "MEMORY")
public class InMemoryTagRepository implements TagRepository {

    private final AtomicLong idCounter = new AtomicLong();
    private final Map<Long, Tag> tagsById = new ConcurrentHashMap<>();
    private final Map<String, Long> idsByKey = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> subscriptionToTagIds = new ConcurrentHashMap<>();

    @Override
    public boolean addTag(Tag tag) {
        String key = key(tag.getChat().getId(), tag.getName());
        if (idsByKey.containsKey(key)) {
            return false;
        }

        long id = idCounter.incrementAndGet();
        Tag stored = new Tag(tag.getChat(), tag.getName());
        stored.setId(id);

        tagsById.put(id, stored);
        idsByKey.put(key, id);
        tag.setId(id);
        return true;
    }

    @Override
    public Optional<Tag> findById(long id) {
        return Optional.ofNullable(tagsById.get(id));
    }

    @Override
    public Optional<Tag> findByChatIdAndName(long chatId, String name) {
        Long id = idsByKey.get(key(chatId, name));
        return id == null ? Optional.empty() : Optional.ofNullable(tagsById.get(id));
    }

    @Override
    public List<Tag> findAllByChat(Chat chat) {
        return tagsById.values().stream()
                .filter(tag -> tag.getChat().getId() == chat.getId())
                .sorted(java.util.Comparator.comparing(Tag::getId))
                .toList();
    }

    @Override
    public List<Tag> findAllBySubscription(long subscriptionId) {
        return subscriptionToTagIds.getOrDefault(subscriptionId, Set.of()).stream()
                .map(tagsById::get)
                .filter(java.util.Objects::nonNull)
                .sorted(java.util.Comparator.comparing(Tag::getId))
                .toList();
    }

    @Override
    public boolean updateName(long tagId, String newName) {
        Tag tag = tagsById.get(tagId);
        if (tag == null) {
            return false;
        }

        String newKey = key(tag.getChat().getId(), newName);
        if (idsByKey.containsKey(newKey)) {
            return false;
        }

        idsByKey.remove(key(tag.getChat().getId(), tag.getName()));
        tag.setName(newName);
        idsByKey.put(newKey, tagId);
        return true;
    }

    @Override
    public boolean deleteById(long id) {
        Tag removed = tagsById.remove(id);
        if (removed == null) {
            return false;
        }

        idsByKey.remove(key(removed.getChat().getId(), removed.getName()));
        subscriptionToTagIds.values().forEach(ids -> ids.remove(id));
        return true;
    }

    @Override
    public boolean bindToSubscription(long subscriptionId, long tagId) {
        if (!tagsById.containsKey(tagId)) {
            return false;
        }

        return subscriptionToTagIds
                .computeIfAbsent(subscriptionId, ignored -> ConcurrentHashMap.newKeySet())
                .add(tagId);
    }

    @Override
    public boolean unbindFromSubscription(long subscriptionId, long tagId) {
        Set<Long> ids = subscriptionToTagIds.get(subscriptionId);
        return ids != null && ids.remove(tagId);
    }

    private String key(long chatId, String name) {
        return chatId + "::" + name;
    }
}
