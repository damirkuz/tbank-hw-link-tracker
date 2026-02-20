package backend.academy.linktracker.bot.handler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Order(1)
public class StartCommandHandler implements Handler {

    private final TelegramBot bot;

    @Override
    public boolean canHandle(Update update) {
        return update.message().text() != null &&
            update.message().text().startsWith("/start");
    }

    @Override
    public void handle(Update update) {
        long chatId = update.message().chat().id();
        SendMessage answer = new SendMessage(chatId, "Привет, напиши /help, чтобы посмотреть доступные команды");

        bot.execute(answer);
    }
}
