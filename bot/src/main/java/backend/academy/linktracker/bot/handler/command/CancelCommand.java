package backend.academy.linktracker.bot.handler.command;

import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("/cancel")
@RequiredArgsConstructor
public class CancelCommand implements CommandHandler {

    private final BotOperations botOperations;
    private final BotTextService botTextService;

    @Override
    public boolean isCancelStateCommand() {
        return true;
    }

    @Override
    public void handle(Update update) {
        long chatId = update.message().chat().id();
        botOperations.sendMessage(chatId, botTextService.get("bot.common.cancelled"));
    }
}
