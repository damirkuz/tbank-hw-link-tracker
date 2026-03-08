package backend.academy.linktracker.bot.handler.command;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.common.exception.ChatAlreadyExistsException;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("/start")
@RequiredArgsConstructor
public class StartCommandHandler implements CommandHandler {

    private final ScrapperClient scrapperClient;

    private final BotOperations botOperations;
    private final BotTextService botTextService;

    @Override
    public void handle(Update update) {
        long chatId = update.message().chat().id();

        String answer = botTextService.get("bot.common.start");

        botOperations.sendMessage(chatId, answer);
        try {
            scrapperClient.registerChat(chatId);
        } catch (ChatAlreadyExistsException _) {
            // игнорируем
        }
    }

    @Override
    public boolean isCancelStateCommand() {
        return true;
    }
}
