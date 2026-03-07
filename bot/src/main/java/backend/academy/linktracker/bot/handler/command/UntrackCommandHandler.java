package backend.academy.linktracker.bot.handler.command;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.util.StringParser;
import backend.academy.linktracker.bot.validator.link.LinkValidationResult;
import backend.academy.linktracker.bot.validator.link.LinkValidationService;
import backend.academy.linktracker.common.exception.ChatNotFoundException;
import backend.academy.linktracker.common.request.RemoveLinkRequest;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UntrackCommandHandler implements CommandHandler {

    private final ScrapperClient scrapperClient;
    private final BotTextService botTextService;
    private final BotOperations botOperations;
    private final StringParser stringParser;
    private final LinkValidationService linkValidationService;

    @Override
    public boolean isCancelStateCommand() {
        return true;
    }

    @Override
    public void handle(Update update) {
        long chatId = update.message().chat().id();

        String link = stringParser.parseAfterSpace(update.message().text());
        LinkValidationResult linkValidationResult = linkValidationService.validate(link);

        if (!linkValidationResult.valid()) {
            botOperations.sendMessage(chatId, botTextService.get("bot.track.invalid-link"));
            return;
        }

        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(link);
        String answer;
        try {
            scrapperClient.deleteLink(chatId, removeLinkRequest);
            answer = botTextService.get("bot.untrack.success");
        } catch (ChatNotFoundException e) {
            answer = botTextService.get("bot.untrack.chat-or-link-not-found");
        }

        botOperations.sendMessage(chatId, answer);
    }
}
