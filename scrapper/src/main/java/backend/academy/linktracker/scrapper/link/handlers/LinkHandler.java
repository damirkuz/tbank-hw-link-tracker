package backend.academy.linktracker.scrapper.link.handlers;

import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.util.Set;

public interface LinkHandler {
    Set<String> supportedHosts();

    TrackedResource resource();
}
