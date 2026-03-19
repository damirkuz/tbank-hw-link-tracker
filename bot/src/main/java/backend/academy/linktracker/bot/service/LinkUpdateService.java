package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.contracts.dto.request.CommonLinkUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkUpdateService {

    private final BotOperations botOperations;
    private final BotTextService botTextService;

    public void handleLinkUpdate(CommonLinkUpdate linkUpdate) {
        for (long chatId : linkUpdate.tgChatIds()) {
            botOperations.sendMessage(
                    chatId,
                    botTextService.get(
                            "bot.track.link-update",
                            String.valueOf(linkUpdate.id()),
                            linkUpdate.url(),
                            linkUpdate.description()));
        }
    }
}
