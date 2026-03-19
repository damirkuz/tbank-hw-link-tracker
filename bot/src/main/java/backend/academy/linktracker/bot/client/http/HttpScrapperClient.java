package backend.academy.linktracker.bot.client.http;

import backend.academy.linktracker.bot.client.ScrapperGateway;
import backend.academy.linktracker.contracts.dto.mapper.ScrapperHttpMapper;
import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.generated.client.DefaultApi;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
@ConditionalOnProperty(prefix = "scrapper", name = "transport", havingValue = "http")
@RequiredArgsConstructor
public class HttpScrapperClient implements ScrapperGateway {

    private final DefaultApi scrapperApi;

    @Override
    public void registerChat(long chatId) {
        try {
            scrapperApi.tgChatIdPost(chatId);
        } catch (RestClientResponseException e) {
            throw mapRegisterChatException(e);
        }
    }

    @Override
    public void deleteChat(long chatId) {
        try {
            scrapperApi.tgChatIdDelete(chatId);
        } catch (RestClientResponseException e) {
            throw mapDeleteChatException(e);
        }
    }

    @Override
    public void addLink(long chatId, CommonAddLinkRequest commonAddLinkRequest) {
        try {
            scrapperApi.linksPost(chatId, ScrapperHttpMapper.toAddLinkRequest(commonAddLinkRequest));
        } catch (RestClientResponseException e) {
            throw mapAddLinkException(chatId, e);
        }
    }

    @Override
    public void deleteLink(long chatId, CommonRemoveLinkRequest commonRemoveLinkRequest) {
        try {
            scrapperApi.linksDelete(chatId, ScrapperHttpMapper.toRemoveLinkRequest(commonRemoveLinkRequest));
        } catch (RestClientResponseException e) {
            throw mapDeleteLinkException(e);
        }
    }

    @Override
    public CommonListLinksResponse getLinks(long chatId) {
        try {
            var response = scrapperApi.linksGet(chatId);
            var body = response.getBody();

            if (body == null) {
                throw new IllegalStateException("Scrapper вернул пустое тело ответа");
            }

            return ScrapperHttpMapper.fromListLinksResponse(body);
        } catch (RestClientResponseException e) {
            throw mapGetLinksException(e);
        }
    }

    private RuntimeException mapRegisterChatException(RestClientResponseException e) {
        return switch (e.getStatusCode().value()) {
            case 400 -> badRequestException();
            case 409 -> new ChatAlreadyExistsException();
            default -> e;
        };
    }

    private RuntimeException mapDeleteChatException(RestClientResponseException e) {
        return switch (e.getStatusCode().value()) {
            case 400 -> badRequestException();
            case 404 -> new ChatNotFoundException();
            default -> e;
        };
    }

    private RuntimeException mapAddLinkException(long chatId, RestClientResponseException e) {
        return switch (e.getStatusCode().value()) {
            case 400 -> badRequestException();
            case 404 -> new ChatNotFoundException("Не найден чат: " + chatId);
            case 409 -> new LinkAlreadyTrackedException();
            default -> e;
        };
    }

    private RuntimeException mapDeleteLinkException(RestClientResponseException e) {
        return switch (e.getStatusCode().value()) {
            case 400 -> badRequestException();
            case 404 -> new ChatNotFoundException();
            default -> e;
        };
    }

    private RuntimeException mapGetLinksException(RestClientResponseException e) {
        return switch (e.getStatusCode().value()) {
            case 400 -> badRequestException();
            case 404 -> new ChatNotFoundException();
            default -> e;
        };
    }

    @SneakyThrows
    private RuntimeException badRequestException() {
        throw new BadRequestException();
    }
}
