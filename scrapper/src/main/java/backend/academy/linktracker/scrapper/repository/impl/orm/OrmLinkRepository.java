package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.jpa.entity.LinkEntity;
import backend.academy.linktracker.scrapper.repository.jpa.mapper.LinkEntityMapper;
import backend.academy.linktracker.scrapper.repository.jpa.repository.LinkJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
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
    public List<Link> getLinks() {
        return linkJpaRepository.findAll()
            .stream()
            .map(linkEntityMapper::toDomain)
            .toList();
    }

    @Override
    public void addLink(Link link) {
        try {
            linkJpaRepository.saveAndFlush(newLinkEntity(link));
        } catch (DataIntegrityViolationException ignored) {
        }
    }

    private LinkEntity newLinkEntity(Link link) {
        LinkEntity entity = new LinkEntity();
        entity.setUri(link.getUri());
        entity.setTrackedResource(link.getTrackedResource());
        entity.setLastUpdate(link.getLastUpdate());
        return entity;
    }
}
