package backend.academy.linktracker.scrapper.client.module.http;

import backend.academy.linktracker.common.request.LinkUpdate;
import backend.academy.linktracker.scrapper.client.module.BotGateway;
import backend.academy.linktracker.scrapper.properties.BotProperties;
import org.apache.coyote.BadRequestException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(prefix = "bot", name = "transport", havingValue = "http")
public class HttpBotClient implements BotGateway {

    private final RestClient restClient;

    public HttpBotClient(RestClient.Builder restClientBuilder, BotProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.http().baseUrl()).build();
    }

    @Override
    public void sendUpdate(LinkUpdate linkUpdate) {
        restClient
                .post()
                .uri("/updates")
                .body(linkUpdate)
                .retrieve()
                .onStatus(status -> status.value() == 400, (request, response) -> {
                    throw new BadRequestException();
                })
                .toBodilessEntity();
    }
}
