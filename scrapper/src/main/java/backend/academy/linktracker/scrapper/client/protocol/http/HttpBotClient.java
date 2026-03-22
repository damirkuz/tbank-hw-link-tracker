package backend.academy.linktracker.scrapper.client.protocol.http;

import backend.academy.linktracker.bot.generated.client.DefaultApi;
import backend.academy.linktracker.contracts.dto.mapper.BotHttpMapper;
import backend.academy.linktracker.contracts.dto.request.CommonLinkUpdate;
import backend.academy.linktracker.scrapper.client.protocol.BotGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "bot", name = "transport", havingValue = "http")
@RequiredArgsConstructor
public class HttpBotClient implements BotGateway {

    private final DefaultApi botApi;

    @Override
    public void sendUpdate(CommonLinkUpdate commonLinkUpdate) {
        try {
            botApi.updatesPost(BotHttpMapper.toLinkUpdate(commonLinkUpdate));
        } catch (RestClientResponseException e) {
            throw mapSendUpdateException(e);
        }
    }

    private RuntimeException mapSendUpdateException(RestClientResponseException e) {
        return switch (e.getStatusCode().value()) {
            case 400 -> {
                logExpected("sendUpdate", e);
                yield new IllegalArgumentException(extractMessage(e), e);
            }
            default -> {
                logUnexpected("sendUpdate", e);
                yield e;
            }
        };
    }

    private void logExpected(String operation, RestClientResponseException e) {
        log.atWarn()
                .addKeyValue("client", "bot")
                .addKeyValue("transport", "http")
                .addKeyValue("operation", operation)
                .addKeyValue("http_status", e.getStatusCode().value())
                .addKeyValue("status_text", e.getStatusText())
                .addKeyValue("response_body", e.getResponseBodyAsString())
                .log("Ожидаемая ошибка при вызове bot");
    }

    private void logUnexpected(String operation, RestClientResponseException e) {
        log.atError()
                .setCause(e)
                .addKeyValue("client", "bot")
                .addKeyValue("transport", "http")
                .addKeyValue("operation", operation)
                .addKeyValue("http_status", e.getStatusCode().value())
                .addKeyValue("status_text", e.getStatusText())
                .addKeyValue("response_body", e.getResponseBodyAsString())
                .log("Неожиданная ошибка при вызове bot");
    }

    private String extractMessage(RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        return (body == null || body.isBlank()) ? e.getStatusText() : body;
    }
}
