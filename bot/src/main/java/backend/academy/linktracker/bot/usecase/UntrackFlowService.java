package backend.academy.linktracker.bot.usecase;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.validator.link.LinkValidationService;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UntrackFlowService {

    private final LinkValidationService linkValidationService;
    private final BotTextService botTextService;
    private final BotOperations botOperations;
    private final StateStorage stateStorage;
    private final ScrapperGateway scrapperGateway;

    public void handleLinkInput(UpdateContext updateContext, String rawLink) {
        long chatId = updateContext.chatId();
        String link = rawLink.trim();

        if (!linkValidationService.validate(link).valid()) {
            botOperations.sendMessage(chatId, botTextService.get("bot.track.invalid-link"));
            return;
        }

        String answer;
        try {
            scrapperGateway.deleteLink(chatId, new CommonRemoveLinkRequest(URI.create(link)));
            answer = botTextService.get("bot.untrack.success");
        } catch (ChatNotFoundException e) {
            answer = botTextService.get("bot.untrack.chat-or-link-not-found");
        }

        stateStorage.clearState(updateContext.requireUserChatKey());
        botOperations.sendMessage(chatId, answer);
    }
}
