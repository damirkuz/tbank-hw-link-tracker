package backend.academy.linktracker.bot.controller.grpc;

import backend.academy.linktracker.contracts.dto.request.LinkUpdate;

public interface BotGrpcUpdateHandler {
    void handle(LinkUpdate linkUpdate);
}
