package backend.academy.linktracker.bot.controller.grpc;

import backend.academy.linktracker.bot.service.LinkUpdateService;
import backend.academy.linktracker.contracts.dto.request.CommonLinkUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultBotGrpcUpdateHandler implements BotGrpcUpdateHandler {

    private final LinkUpdateService linkUpdateService;

    @Override
    public void handle(CommonLinkUpdate commonLinkUpdate) {
        linkUpdateService.handleLinkUpdate(commonLinkUpdate);
    }
}
