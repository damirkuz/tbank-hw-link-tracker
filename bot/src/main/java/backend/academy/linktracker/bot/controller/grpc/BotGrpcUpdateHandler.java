package backend.academy.linktracker.bot.controller.grpc;

import backend.academy.linktracker.contracts.dto.request.CommonLinkUpdate;

public interface BotGrpcUpdateHandler {
    void handle(CommonLinkUpdate commonLinkUpdate);
}
