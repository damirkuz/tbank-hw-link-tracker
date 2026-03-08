package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.properties.ScrapperProperties;
import backend.academy.linktracker.common.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.common.exception.ChatNotFoundException;
import backend.academy.linktracker.common.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.common.request.AddLinkRequest;
import backend.academy.linktracker.common.request.RemoveLinkRequest;
import backend.academy.linktracker.common.response.LinkResponse;
import backend.academy.linktracker.common.response.ListLinksResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ScrapperClient {

    private final RestClient restClient;

    public ScrapperClient(RestClient.Builder restClientBuilder, ScrapperProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
    }

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

    public LinkResponse addLink(long chatId, AddLinkRequest addLinkRequest) {
        return restClient
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
                .body(LinkResponse.class);
    }

    public LinkResponse deleteLink(long chatId, RemoveLinkRequest removeLinkRequest) {
        return restClient
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
                .body(LinkResponse.class);
    }

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
