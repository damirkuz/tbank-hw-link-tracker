package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Filter;
import java.util.List;
import java.util.Optional;

public interface FilterRepository {

    boolean addFilter(Filter filter);

    Optional<Filter> findById(long id);

    Optional<Filter> findByChatIdAndValue(long chatId, String value);

    List<Filter> findAllByChat(Chat chat);

    List<Filter> findAllBySubscription(long subscriptionId);

    boolean updateValue(long filterId, String newValue);

    boolean deleteById(long id);

    boolean bindToSubscription(long subscriptionId, long filterId);

    boolean unbindFromSubscription(long subscriptionId, long filterId);
}
