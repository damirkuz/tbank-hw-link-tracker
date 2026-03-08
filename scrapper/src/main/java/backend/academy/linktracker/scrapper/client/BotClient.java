package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.common.request.LinkUpdate;
import backend.academy.linktracker.scrapper.properties.BotProperties;
import org.apache.coyote.BadRequestException;
import org.springframework.web.client.RestClient;

public class BotClient {

    private final RestClient restClient;

    public BotClient(RestClient.Builder restClientBuilder, BotProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
    }

    public void sendUpdate(LinkUpdate linkUpdate) {
        restClient
                .post()
                .uri("/updates")
                .body(linkUpdate)
                .retrieve()
                .onStatus(status -> status.value() == 400, (request, response) -> {
                    throw new BadRequestException();
                });
    }
}
