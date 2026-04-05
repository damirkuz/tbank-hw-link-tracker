package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.jpa.entity.LinkEntity;
import backend.academy.linktracker.scrapper.repository.jpa.mapper.LinkEntityMapper;
import backend.academy.linktracker.scrapper.repository.jpa.repository.LinkJpaRepository;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "ORM")
public class OrmLinkRepository implements LinkRepository {

    private final LinkJpaRepository linkJpaRepository;
    private final LinkEntityMapper linkEntityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Link> findLinksForUpdate(OffsetDateTime before, long lastSeenId, int limit) {
        return linkJpaRepository
                .findLinksForUpdate(before, lastSeenId, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id")))
                .stream()
                .map(linkEntityMapper::toDomain)
                .toList();
    }

    @Override
    public void addLink(Link link) {
        linkJpaRepository
                .findByUriAndTrackedResource(link.getUri(), link.getTrackedResource())
                .ifPresentOrElse(existing -> link.setId(existing.getId()), () -> {
                    LinkEntity saved = linkJpaRepository.save(newLinkEntity(link));
                    link.setId(saved.getId());
                });
    }

    @Override
    public void updateCheckState(long linkId, Instant lastUpdate, OffsetDateTime nextCheckAt) {
        linkJpaRepository.updateCheckState(linkId, lastUpdate, nextCheckAt);
    }

    @Override
    public void updateNextCheckAt(long linkId, OffsetDateTime nextCheckAt) {
        linkJpaRepository.updateNextCheckAt(linkId, nextCheckAt);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Link> findById(long id) {
        return linkJpaRepository.findById(id).map(linkEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Link> findByUriAndTrackedResource(URI uri, TrackedResource trackedResource) {
        return linkJpaRepository
                .findByUriAndTrackedResource(uri, trackedResource)
                .map(linkEntityMapper::toDomain);
    }

    @Override
    public boolean deleteById(long id) {
        if (!linkJpaRepository.existsById(id)) {
            return false;
        }

        linkJpaRepository.deleteById(id);
        linkJpaRepository.flush();
        return true;
    }

    private LinkEntity newLinkEntity(Link link) {
        LinkEntity entity = new LinkEntity();
        entity.setUri(link.getUri());
        entity.setTrackedResource(link.getTrackedResource());
        entity.setLastUpdate(link.getLastUpdate());
        entity.setNextCheckAt(link.getNextCheckAt());
        return entity;
    }
}
