package backend.academy.linktracker.bot.update.handler.command;

import backend.academy.linktracker.bot.client.ScrapperGateway;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("/start")
@RequiredArgsConstructor
@Slf4j
public class StartCommandHandler implements CommandHandler {

    private final ScrapperGateway scrapperClient;

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
            log.atDebug().addKeyValue("chat_id", chatId).log("Чат уже был зарегистрирован в scrapper");
        }
    }

    @Override
    public boolean isCancelStateCommand() {
        return true;
    }
}
