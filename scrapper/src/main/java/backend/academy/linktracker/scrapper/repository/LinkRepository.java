package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Link;
import java.util.List;

public interface LinkRepository {

    List<Link> getLinks();

    void addLink(Link link);
}
