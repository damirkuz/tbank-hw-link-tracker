package backend.academy.linktracker.scrapper.repository.impl.memory;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Filter;
import backend.academy.linktracker.scrapper.repository.FilterRepository;
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
public class InMemoryFilterRepository implements FilterRepository {

    private final AtomicLong idCounter = new AtomicLong();
    private final Map<Long, Filter> filtersById = new ConcurrentHashMap<>();
    private final Map<String, Long> idsByKey = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> subscriptionToFilterIds = new ConcurrentHashMap<>();

    @Override
    public boolean addFilter(Filter filter) {
        String key = key(filter.getChat().getId(), filter.getValue());
        if (idsByKey.containsKey(key)) {
            return false;
        }

        long id = idCounter.incrementAndGet();
        Filter stored = new Filter(filter.getChat(), filter.getValue());
        stored.setId(id);

        filtersById.put(id, stored);
        idsByKey.put(key, id);
        filter.setId(id);
        return true;
    }

    @Override
    public Optional<Filter> findById(long id) {
        return Optional.ofNullable(filtersById.get(id));
    }

    @Override
    public Optional<Filter> findByChatIdAndValue(long chatId, String value) {
        Long id = idsByKey.get(key(chatId, value));
        return id == null ? Optional.empty() : Optional.ofNullable(filtersById.get(id));
    }

    @Override
    public List<Filter> findAllByChat(Chat chat) {
        return filtersById.values().stream()
                .filter(filter -> filter.getChat().getId() == chat.getId())
                .sorted(java.util.Comparator.comparing(Filter::getId))
                .toList();
    }

    @Override
    public List<Filter> findAllBySubscription(long subscriptionId) {
        return subscriptionToFilterIds.getOrDefault(subscriptionId, Set.of()).stream()
                .map(filtersById::get)
                .filter(java.util.Objects::nonNull)
                .sorted(java.util.Comparator.comparing(Filter::getId))
                .toList();
    }

    @Override
    public boolean updateValue(long filterId, String newValue) {
        Filter filter = filtersById.get(filterId);
        if (filter == null) {
            return false;
        }

        String newKey = key(filter.getChat().getId(), newValue);
        if (idsByKey.containsKey(newKey)) {
            return false;
        }

        idsByKey.remove(key(filter.getChat().getId(), filter.getValue()));
        filter.setValue(newValue);
        idsByKey.put(newKey, filterId);
        return true;
    }

    @Override
    public boolean deleteById(long id) {
        Filter removed = filtersById.remove(id);
        if (removed == null) {
            return false;
        }

        idsByKey.remove(key(removed.getChat().getId(), removed.getValue()));
        subscriptionToFilterIds.values().forEach(ids -> ids.remove(id));
        return true;
    }

    @Override
    public boolean bindToSubscription(long subscriptionId, long filterId) {
        if (!filtersById.containsKey(filterId)) {
            return false;
        }

        return subscriptionToFilterIds
                .computeIfAbsent(subscriptionId, ignored -> ConcurrentHashMap.newKeySet())
                .add(filterId);
    }

    @Override
    public boolean unbindFromSubscription(long subscriptionId, long filterId) {
        Set<Long> ids = subscriptionToFilterIds.get(subscriptionId);
        return ids != null && ids.remove(filterId);
    }

    private String key(long chatId, String value) {
        return chatId + "::" + value;
    }
}
