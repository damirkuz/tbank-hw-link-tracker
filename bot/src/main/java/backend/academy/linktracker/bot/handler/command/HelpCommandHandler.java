package backend.academy.linktracker.bot.handler.command;

import backend.academy.linktracker.bot.context.BotContext;
import backend.academy.linktracker.bot.service.BotOperations;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HelpCommandHandler implements CommandHandler {

    private final BotContext botContext;
    private final BotOperations botOperations;

    @Override
    public boolean canHandle(Update update) {
        return update.message().text() != null &&
            update.message().text().startsWith("/help");
    }

    @Override
    public void handle(Update update) {
        long chatId = update.message().chat().id();
        botOperations.sendMessage(chatId, botContext.messageProperties().helpCommand());
    }

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public boolean isCancelStateCommand() {
        return false;
    }
}
