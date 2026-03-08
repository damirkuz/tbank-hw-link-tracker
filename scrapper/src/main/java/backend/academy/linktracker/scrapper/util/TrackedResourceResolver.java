package backend.academy.linktracker.scrapper.util;

import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.net.URI;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class TrackedResourceResolver {

    public TrackedResource resolve(URI uri) {
        String host = normalizeHost(uri);

        return switch (host) {
            case "github.com" -> TrackedResource.GITHUB;
            case "stackoverflow.com", "ru.stackoverflow.com" -> TrackedResource.STACKOVERFLOW;
            default -> throw new IllegalArgumentException("Unsupported tracked resource: " + uri);
        };
    }

    public TrackedResource resolve(String rawUri) {
        return resolve(URI.create(rawUri));
    }

    private String normalizeHost(URI uri) {
        String host = uri.getHost();

        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("URI does not contain host: " + uri);
        }

        host = host.toLowerCase(Locale.ROOT);

        if (host.startsWith("www.")) {
            host = host.substring(4);
        }

        return host;
    }
}
