package backend.academy.linktracker.bot.update.router;

import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Update;

public interface Router {

    void route(Update update, UpdateContext updateContext);
}
