package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Tag;
import java.util.List;
import java.util.Optional;

public interface TagRepository {

    boolean addTag(Tag tag);

    Optional<Tag> findById(long id);

    Optional<Tag> findByChatIdAndName(long chatId, String name);

    List<Tag> findAllByChat(Chat chat);

    List<Tag> findAllBySubscription(long subscriptionId);

    boolean updateName(long tagId, String newName);

    boolean deleteById(long id);

    boolean bindToSubscription(long subscriptionId, long tagId);

    boolean unbindFromSubscription(long subscriptionId, long tagId);
}
