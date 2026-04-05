package backend.academy.linktracker.bot.update.handler.state;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.validator.link.LinkValidationResult;
import backend.academy.linktracker.bot.validator.link.LinkValidationService;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import com.pengrad.telegrambot.model.Update;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UntrackWaitLinkHandler implements StateHandler {

    private final LinkValidationService linkValidationService;
    private final BotTextService botTextService;
    private final BotOperations botOperations;
    private final StateStorage stateStorage;
    private final ScrapperGateway scrapperClient;

    @Override
    public UserState getHandledState() {
        return UserState.UNTRACK_WAIT_LINK;
    }

    @Override
    public void handle(Update update, UpdateContext updateContext) {

        long chatId = updateContext.chatId();

        String link = update.message().text().trim();

        LinkValidationResult linkValidationResult = linkValidationService.validate(link);

        if (!linkValidationResult.valid()) {
            botOperations.sendMessage(chatId, botTextService.get("bot.track.invalid-link"));
            return;
        }

        CommonRemoveLinkRequest commonRemoveLinkRequest = new CommonRemoveLinkRequest(URI.create(link));
        String answer;
        try {
            scrapperClient.deleteLink(chatId, commonRemoveLinkRequest);
            answer = botTextService.get("bot.untrack.success");
        } catch (ChatNotFoundException e) {
            answer = botTextService.get("bot.untrack.chat-or-link-not-found");
        }

        botOperations.sendMessage(chatId, answer);
    }
}
