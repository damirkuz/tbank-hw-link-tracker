package backend.academy.linktracker.bot.update.router;

import com.pengrad.telegrambot.model.Update;

public interface Router {

    void route(Update update);
}
