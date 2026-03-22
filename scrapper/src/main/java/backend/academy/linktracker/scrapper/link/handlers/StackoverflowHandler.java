package backend.academy.linktracker.scrapper.link.handlers;

import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class StackoverflowHandler implements LinkHandler {
    @Override
    public Set<String> supportedHosts() {
        return Set.of("stackoverflow.com", "ru.stackoverflow.com");
    }

    @Override
    public TrackedResource resource() {
        return TrackedResource.STACKOVERFLOW;
    }
}
