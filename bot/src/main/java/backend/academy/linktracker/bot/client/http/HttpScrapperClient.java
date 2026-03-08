package backend.academy.linktracker.bot.client.http;

import backend.academy.linktracker.bot.client.ScrapperGateway;
import backend.academy.linktracker.bot.properties.ScrapperProperties;
import backend.academy.linktracker.common.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.common.exception.ChatNotFoundException;
import backend.academy.linktracker.common.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.common.request.AddLinkRequest;
import backend.academy.linktracker.common.request.RemoveLinkRequest;
import backend.academy.linktracker.common.response.ListLinksResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(prefix = "scrapper", name = "transport", havingValue = "http")
public class HttpScrapperClient implements ScrapperGateway {

    private final RestClient restClient;

    public HttpScrapperClient(RestClient.Builder restClientBuilder, ScrapperProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.http().baseUrl()).build();
    }

    @Override
    public void registerChat(long chatId) {
        restClient
                .post()
                .uri("/tg-chat/" + chatId)
                .retrieve()
                .onStatus(status -> status.value() == 400, (request, response) -> {
                    throw new BadRequestException();
                })
                .onStatus(status -> status.value() == 409, (request, response) -> {
                    throw new ChatAlreadyExistsException();
                })
                .toBodilessEntity();
    }

    @Override
    public void deleteChat(long chatId) {
        restClient
                .delete()
                .uri("/tg-chat/" + chatId)
                .retrieve()
                .onStatus(status -> status.value() == 400, (request, response) -> {
                    throw new BadRequestException();
                })
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new ChatNotFoundException();
                })
                .toBodilessEntity();
    }

    @Override
    public void addLink(long chatId, AddLinkRequest addLinkRequest) {
        restClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(addLinkRequest)
                .retrieve()
                .onStatus(status -> status.value() == 400, (request, response) -> {
                    throw new BadRequestException();
                })
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new ChatNotFoundException("Не найден чат: " + chatId);
                })
                .onStatus(status -> status.value() == 409, (request, response) -> {
                    throw new LinkAlreadyTrackedException();
                })
                .toBodilessEntity();
    }

    @Override
    public void deleteLink(long chatId, RemoveLinkRequest removeLinkRequest) {
        restClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(removeLinkRequest)
                .retrieve()
                .onStatus(status -> status.value() == 400, (request, response) -> {
                    throw new BadRequestException();
                })
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new ChatNotFoundException();
                })
                .toBodilessEntity();
    }

    @Override
    public ListLinksResponse getLinks(long chatId) {
        return restClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .onStatus(status -> status.value() == 400, (request, response) -> {
                    throw new BadRequestException();
                })
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new ChatNotFoundException();
                })
                .body(ListLinksResponse.class);
    }
}
