package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Link;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LinkRepository {

    private final AtomicLong idCounter = new AtomicLong();

    private final Set<Link> links = ConcurrentHashMap.newKeySet();

    public List<Link> getLinks() {
        return links.stream().toList();
    }

    // метод не будет вызывать ошибки, если ссылка уже есть
    public void addLink(Link link) {
        boolean added = links.add(link);
        if (added) {
            link.setId(idCounter.incrementAndGet());
        }
    }
}
