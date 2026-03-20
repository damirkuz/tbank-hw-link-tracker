package backend.academy.linktracker.scrapper.link.handlers;

import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.util.Set;

public class GithubLinkHandler implements LinkHandler {

    @Override
    public Set<String> supportedHosts() {
        return Set.of("github.com");
    }

    @Override
    public TrackedResource resource() {
        return TrackedResource.GITHUB;
    }
}
