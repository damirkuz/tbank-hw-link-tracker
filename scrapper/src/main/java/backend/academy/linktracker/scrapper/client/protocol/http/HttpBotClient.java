package backend.academy.linktracker.scrapper.client.protocol.http;

import backend.academy.linktracker.bot.generated.client.DefaultApi;
import backend.academy.linktracker.contracts.dto.mapper.BotHttpMapper;
import backend.academy.linktracker.contracts.dto.request.CommonLinkUpdate;
import backend.academy.linktracker.scrapper.client.protocol.BotGateway;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

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
            case 400 -> badRequestException();
            default -> e;
        };
    }

    @SneakyThrows
    private RuntimeException badRequestException() {
        throw new BadRequestException();
    }
}
