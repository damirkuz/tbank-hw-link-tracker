package backend.academy.linktracker.bot.update.handler;

import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Update;

public interface Handler {
    void handle(Update update, UpdateContext updateContext);
}
