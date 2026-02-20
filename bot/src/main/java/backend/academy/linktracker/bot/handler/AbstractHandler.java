package backend.academy.linktracker.bot.handler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import lombok.AllArgsConstructor;


public abstract class AbstractHandler {

    protected final TelegramBot bot;

    protected AbstractHandler(TelegramBot bot) {
        this.bot = bot;
    }

    public abstract boolean canHandle(Update update);

    public abstract void handle(Update update);

}
