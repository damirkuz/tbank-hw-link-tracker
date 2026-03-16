package backend.academy.linktracker.bot.update.handler;

import com.pengrad.telegrambot.model.Update;

public interface Handler {
    void handle(Update update);
}
