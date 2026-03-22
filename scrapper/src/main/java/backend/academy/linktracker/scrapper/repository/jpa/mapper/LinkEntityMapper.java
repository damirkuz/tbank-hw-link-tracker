package backend.academy.linktracker.scrapper.repository.jpa.mapper;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.jpa.entity.LinkEntity;
import org.springframework.stereotype.Component;

@Component
public class LinkEntityMapper {

    public LinkEntity toEntity(Link link) {
        LinkEntity entity = new LinkEntity();
        entity.setId(link.getId());
        entity.setUri(link.getUri());
        entity.setTrackedResource(link.getTrackedResource());
        entity.setLastUpdate(link.getLastUpdate());
        return entity;
    }

    public Link toDomain(LinkEntity entity) {
        Link link = new Link(entity.getUri(), entity.getTrackedResource());
        link.setId(entity.getId());
        link.setLastUpdate(entity.getLastUpdate());
        return link;
    }
}
