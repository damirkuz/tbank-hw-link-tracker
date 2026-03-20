package backend.academy.linktracker.scrapper.link;

import backend.academy.linktracker.scrapper.link.handlers.LinkHandler;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public final class TrackedResourceResolver {

    private final Map<String, LinkHandler> handlersByHost;

    public TrackedResourceResolver(List<LinkHandler> handlers) {
        Map<String, LinkHandler> map = new LinkedHashMap<>();

        for (LinkHandler handler : handlers) {
            for (String rawHost : handler.supportedHosts()) {
                String normalizedHost = normalizeHost(rawHost);

                LinkHandler existing = map.putIfAbsent(normalizedHost, handler);
                if (existing != null) {
                    log.atError()
                            .addKeyValue("host", normalizedHost)
                            .addKeyValue("existing_resource", existing.resource())
                            .addKeyValue("new_resource", handler.resource())
                            .log("Обнаружен конфликт регистрации обработчиков по host");

                    throw new IllegalStateException("Duplicate handler for host: " + normalizedHost);
                }
            }
        }

        this.handlersByHost = Map.copyOf(map);

        log.atInfo()
                .addKeyValue("registered_hosts_count", handlersByHost.size())
                .addKeyValue("handlers_count", handlers.size())
                .log("TrackedResourceResolver инициализирован");
    }

    public TrackedResource resolve(URI uri) {
        String host = normalizeHost(uri);
        LinkHandler handler = handlersByHost.get(host);

        if (handler == null) {
            log.atWarn().addKeyValue("uri", uri).addKeyValue("host", host).log("Ресурс не поддерживается");

            throw new IllegalArgumentException("Ресурс не поддерживается: " + uri);
        }

        TrackedResource resource = handler.resource();

        log.atDebug()
                .addKeyValue("uri", uri)
                .addKeyValue("host", host)
                .addKeyValue("resource", resource)
                .log("Ресурс успешно определён");

        return resource;
    }

    private String normalizeHost(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI не должен быть null");
        }

        String host = uri.getHost();

        if (host == null || host.isBlank()) {
            log.atWarn().addKeyValue("uri", uri).log("URI не содержит host");

            throw new IllegalArgumentException("URI не содержит host: " + uri);
        }

        return normalizeHost(host);
    }

    private String normalizeHost(String host) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host не должен быть пустым");
        }

        String normalized = host.toLowerCase(Locale.ROOT);

        if (normalized.startsWith("www.")) {
            normalized = normalized.substring(4);
        }

        return normalized;
    }
}
