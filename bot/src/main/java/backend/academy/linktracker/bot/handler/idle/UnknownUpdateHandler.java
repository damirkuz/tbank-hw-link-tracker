package backend.academy.linktracker.bot.handler.idle;

import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnknownUpdateHandler implements IdleHandler {

    private final BotTextService botTextService;
    private final BotOperations botOperations;

    @Override
    public void handle(Update update) {
        long chatId = update.message().chat().id();

        botOperations.sendMessage(chatId, botTextService.get("bot.common.unknown-update"));
    }

    @Override
    public boolean canHandle(Update update) {
        return true;
    }
}
