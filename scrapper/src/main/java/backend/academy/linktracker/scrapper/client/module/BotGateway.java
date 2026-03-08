package backend.academy.linktracker.scrapper.client.module;

import backend.academy.linktracker.common.request.LinkUpdate;

public interface BotGateway {
    void sendUpdate(LinkUpdate linkUpdate);
}
