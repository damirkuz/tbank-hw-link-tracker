package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.common.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.common.exception.ChatNotFoundException;
import backend.academy.linktracker.common.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.common.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.model.Link;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserLinksRepository {

    private final Map<Long, Set<Link>> saved = new ConcurrentHashMap<>();

    public void registerChat(long chatId) {
        Set<Link> added = saved.putIfAbsent(chatId, ConcurrentHashMap.newKeySet());
        if (added != null) {
            throw new ChatAlreadyExistsException();
        }
    }

    public void deleteChat(long chatId) {
        Set<Link> deleted = saved.remove(chatId);
        if (deleted == null) {
            throw new ChatNotFoundException();
        }
    }

    public void addLink(long chatId, Link link) {
        Set<Link> links = getLinkSetFromChatId(chatId);

        boolean added = links.add(link);

        if (!added) {
            throw new LinkAlreadyTrackedException();
        }
    }

    public void deleteLink(long chatId, Link link) {
        Set<Link> links = getLinkSetFromChatId(chatId);

        boolean deleted = links.remove(link);

        if (!deleted) {
            throw new LinkNotFoundException();
        }
    }

    public List<Link> getLinks(long chatId) {
        return getLinkSetFromChatId(chatId).stream().toList();
    }

    private Set<Link> getLinkSetFromChatId(long chatId) {
        Set<Link> links = saved.get(chatId);
        if (links == null) {
            throw new ChatNotFoundException();
        }
        return links;
    }
}
