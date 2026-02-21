package backend.academy.linktracker.bot.handler.idle;

import backend.academy.linktracker.bot.context.BotContext;
import backend.academy.linktracker.bot.service.BotOperations;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnknownUpdateHandler implements IdleHandler {

    private final BotContext botContext;
    private final BotOperations botOperations;

    @Override
    public void handle(Update update) {
        long chatId = update.message().chat().id();
        botOperations.sendMessage(chatId, botContext.messageProperties().unknownUpdate());
    }

    @Override
    public boolean canHandle(Update update) {
        return true;
    }
}
