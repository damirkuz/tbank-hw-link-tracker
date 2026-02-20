package backend.academy.linktracker.bot.handler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MainHandler {

    private final TelegramBot bot;

    @PostConstruct
    public void init() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null) {
                    long chatId = update.message().chat().id();
                    String text = update.message().text();
                    SendMessage message = new SendMessage(chatId, "Эхо: " + text);
                    bot.execute(message);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

    }
}
