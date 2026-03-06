package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.client.exception.ChatNotFoundException;
import backend.academy.linktracker.bot.client.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.bot.client.request.AddLinkRequest;
import backend.academy.linktracker.bot.client.request.RemoveLinkRequest;
import backend.academy.linktracker.bot.client.response.LinkResponse;
import backend.academy.linktracker.bot.client.response.ListLinksResponse;
import backend.academy.linktracker.bot.properties.ScrapperProperties;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ScrapperClient {

    private final RestClient restClient;

    public ScrapperClient(
        RestClient.Builder restClientBuilder,
        ScrapperProperties properties
    ) {
        this.restClient = restClientBuilder
            .baseUrl(properties.baseUrl())
            .build();
    }


    public void registerChat(long chatId) {}

    public void deleteChat(long chatId) {}

    public LinkResponse addLink(long chatId, AddLinkRequest addLinkRequest){

        LinkResponse linkResponse = restClient
            .post()
            .uri("/links")
            .body(addLinkRequest)
            .retrieve()
            .onStatus(status -> status.value() == 400,
                (request, response) -> {throw new BadRequestException();})
            .onStatus(status -> status.value() == 404,
                (request, response) -> {throw new ChatNotFoundException("Не найден чат: " + chatId);})
            .onStatus(status -> status.value() == 409,
                (request, response) -> {throw new LinkAlreadyTrackedException("Ссылка уже отслеживается");})
            .body(LinkResponse.class);

        return linkResponse;
    }

    public LinkResponse deleteLink(long chatId, RemoveLinkRequest removeLinkRequest) {
        return null;
    }

    public ListLinksResponse getLinks(long chatId) {
        return null;
    }
}
