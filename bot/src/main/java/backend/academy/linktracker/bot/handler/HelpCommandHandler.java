package backend.academy.linktracker.bot.handler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(0)
public class HelpCommandHandler extends AbstractHandler {

    protected HelpCommandHandler(TelegramBot bot) {
        super(bot);
    }

    @Override
    public boolean canHandle(Update update) {
        return update.message().text() != null &&
            update.message().text().startsWith("/help");
    }

    @Override
    public void handle(Update update) {
        long chatId = update.message().chat().id();
        SendMessage answer = new SendMessage(chatId, "Доступные команды:\n/start\n/help");

        bot.execute(answer);
    }
}
