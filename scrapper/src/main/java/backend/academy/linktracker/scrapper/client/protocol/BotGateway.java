package backend.academy.linktracker.scrapper.client.protocol;

import backend.academy.linktracker.contracts.dto.request.CommonLinkUpdate;

public interface BotGateway {
    void sendUpdate(CommonLinkUpdate commonLinkUpdate);
}
