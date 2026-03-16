package backend.academy.linktracker.scrapper.client.protocol;

import backend.academy.linktracker.contracts.dto.request.LinkUpdate;

public interface BotGateway {
    void sendUpdate(LinkUpdate linkUpdate);
}
