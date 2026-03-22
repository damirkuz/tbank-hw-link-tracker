package backend.academy.linktracker.bot.update.handler.command;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.util.StringParser;
import backend.academy.linktracker.bot.validator.link.LinkValidationResult;
import backend.academy.linktracker.bot.validator.link.LinkValidationService;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import com.pengrad.telegrambot.model.Update;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("/untrack")
@RequiredArgsConstructor
public class UntrackCommandHandler implements CommandHandler {

    private final ScrapperGateway scrapperClient;
    private final BotTextService botTextService;
    private final BotOperations botOperations;
    private final LinkValidationService linkValidationService;

    @Override
    public void handle(Update update, UpdateContext updateContext) {
        long chatId = updateContext.chatId();

        String link = StringParser.parseAfterSpace(update.message().text());
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
