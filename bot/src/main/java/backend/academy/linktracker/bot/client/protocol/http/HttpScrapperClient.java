package backend.academy.linktracker.bot.client.protocol.http;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.contracts.dto.mapper.ScrapperHttpMapper;
import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.generated.client.DefaultApi;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "scrapper", name = "transport", havingValue = "http")
@RequiredArgsConstructor
public class HttpScrapperClient implements ScrapperGateway {

    private final DefaultApi scrapperApi;

    @Override
    public void registerChat(long chatId) throws ChatAlreadyExistsException {
        try {
            scrapperApi.tgChatIdPost(chatId);
        } catch (RestClientResponseException e) {
            throw mapRegisterChatException(chatId, e);
        }
    }

    @Override
    public void deleteChat(long chatId) throws ChatNotFoundException {
        try {
            scrapperApi.tgChatIdDelete(chatId);
        } catch (RestClientResponseException e) {
            throw mapDeleteChatException(chatId, e);
        }
    }

    @Override
    public void addLink(long chatId, CommonAddLinkRequest request)
            throws ChatNotFoundException, LinkAlreadyTrackedException {
        try {
            scrapperApi.linksPost(chatId, ScrapperHttpMapper.toAddLinkRequest(request));
        } catch (RestClientResponseException e) {
            throw mapAddLinkException(chatId, request, e);
        }
    }

    @Override
    public void deleteLink(long chatId, CommonRemoveLinkRequest request) throws ChatNotFoundException {
        try {
            scrapperApi.linksDelete(chatId, ScrapperHttpMapper.toRemoveLinkRequest(request));
        } catch (RestClientResponseException e) {
            throw mapDeleteLinkException(chatId, request, e);
        }
    }

    @Override
    public CommonListLinksResponse getLinks(long chatId) throws ChatNotFoundException {
        try {
            var response = scrapperApi.linksGet(chatId);
            var body = response.getBody();

            if (body == null) {
                log.atError()
                        .addKeyValue("client", "scrapper")
                        .addKeyValue("transport", "http")
                        .addKeyValue("operation", "getLinks")
                        .addKeyValue("chat_id", chatId)
                        .log("Scrapper вернул пустое тело ответа");
                throw new IllegalStateException("Scrapper вернул пустое тело ответа");
            }

            return ScrapperHttpMapper.fromListLinksResponse(body);
        } catch (RestClientResponseException e) {
            throw mapGetLinksException(chatId, e);
        }
    }

    private RuntimeException mapRegisterChatException(long chatId, RestClientResponseException e)
            throws ChatAlreadyExistsException {
        return switch (e.getStatusCode().value()) {
            case 400 -> {
                logExpected("registerChat", chatId, null, e);
                yield new IllegalArgumentException(extractMessage(e), e);
            }
            case 409 -> {
                logExpected("registerChat", chatId, null, e);
                throw new ChatAlreadyExistsException();
            }
            default -> {
                logUnexpected("registerChat", chatId, null, e);
                yield e;
            }
        };
    }

    private RuntimeException mapDeleteChatException(long chatId, RestClientResponseException e)
            throws ChatNotFoundException {
        return switch (e.getStatusCode().value()) {
            case 400 -> {
                logExpected("deleteChat", chatId, null, e);
                yield new IllegalArgumentException(extractMessage(e), e);
            }
            case 404 -> {
                logExpected("deleteChat", chatId, null, e);
                throw new ChatNotFoundException();
            }
            default -> {
                logUnexpected("deleteChat", chatId, null, e);
                yield e;
            }
        };
    }

    private RuntimeException mapAddLinkException(
            long chatId, CommonAddLinkRequest request, RestClientResponseException e)
            throws ChatNotFoundException, LinkAlreadyTrackedException {
        URI link = request != null ? request.uri() : null;

        return switch (e.getStatusCode().value()) {
            case 400 -> {
                logExpected("addLink", chatId, link, e);
                yield new IllegalArgumentException(extractMessage(e), e);
            }
            case 404 -> {
                logExpected("addLink", chatId, link, e);
                throw new ChatNotFoundException("Не найден чат: " + chatId);
            }
            case 409 -> {
                logExpected("addLink", chatId, link, e);
                throw new LinkAlreadyTrackedException();
            }
            default -> {
                logUnexpected("addLink", chatId, link, e);
                yield e;
            }
        };
    }

    private RuntimeException mapDeleteLinkException(
            long chatId, CommonRemoveLinkRequest request, RestClientResponseException e) throws ChatNotFoundException {
        URI link = request != null ? request.uri() : null;

        return switch (e.getStatusCode().value()) {
            case 400 -> {
                logExpected("deleteLink", chatId, link, e);
                yield new IllegalArgumentException(extractMessage(e), e);
            }
            case 404 -> {
                logExpected("deleteLink", chatId, link, e);
                throw new ChatNotFoundException();
            }
            default -> {
                logUnexpected("deleteLink", chatId, link, e);
                yield e;
            }
        };
    }

    private RuntimeException mapGetLinksException(long chatId, RestClientResponseException e)
            throws ChatNotFoundException {
        return switch (e.getStatusCode().value()) {
            case 400 -> {
                logExpected("getLinks", chatId, null, e);
                yield new IllegalArgumentException(extractMessage(e), e);
            }
            case 404 -> {
                logExpected("getLinks", chatId, null, e);
                throw new ChatNotFoundException();
            }
            default -> {
                logUnexpected("getLinks", chatId, null, e);
                yield e;
            }
        };
    }

    private void logExpected(String operation, long chatId, URI link, RestClientResponseException e) {
        log.atWarn()
                .setCause(e)
                .addKeyValue("client", "scrapper")
                .addKeyValue("transport", "http")
                .addKeyValue("operation", operation)
                .addKeyValue("chat_id", chatId)
                .addKeyValue("link", link)
                .addKeyValue("http_status", e.getStatusCode().value())
                .addKeyValue("status_text", e.getStatusText())
                .addKeyValue("response_body", e.getResponseBodyAsString())
                .log("Ожидаемая ошибка при вызове scrapper");
    }

    private void logUnexpected(String operation, long chatId, URI link, RestClientResponseException e) {
        log.atError()
                .setCause(e)
                .addKeyValue("client", "scrapper")
                .addKeyValue("transport", "http")
                .addKeyValue("operation", operation)
                .addKeyValue("chat_id", chatId)
                .addKeyValue("link", link)
                .addKeyValue("http_status", e.getStatusCode().value())
                .addKeyValue("status_text", e.getStatusText())
                .addKeyValue("response_body", e.getResponseBodyAsString())
                .log("Неожиданная ошибка при вызове scrapper");
    }

    private String extractMessage(RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        return (body == null || body.isBlank()) ? e.getStatusText() : body;
    }
}
