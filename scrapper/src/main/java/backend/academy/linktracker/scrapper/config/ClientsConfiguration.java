package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.client.provider.BaseTrackedClient;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientsConfiguration {

    @Bean
    public Map<TrackedResource, BaseTrackedClient> getClients(List<BaseTrackedClient> clientList) {
        Map<TrackedResource, BaseTrackedClient> clients = new EnumMap<>(TrackedResource.class);

        for (BaseTrackedClient baseTrackedClient : clientList) {
            clients.put(baseTrackedClient.getTrackedResource(), baseTrackedClient);
        }

        return Collections.unmodifiableMap(clients);
    }
}
