package backend.academy.linktracker.bot.update.handler;

import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnknownUpdateHandler implements Handler {

    private final BotTextService botTextService;
    private final BotOperations botOperations;

    @Override
    public void handle(Update update, UpdateContext updateContext) {
        botOperations.sendMessage(updateContext.chatId(), botTextService.get("bot.common.unknown-update"));
    }
}
