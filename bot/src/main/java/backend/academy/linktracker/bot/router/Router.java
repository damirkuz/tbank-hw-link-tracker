package backend.academy.linktracker.bot.router;

import com.pengrad.telegrambot.model.Update;

public interface Router {

    void route(Update update);
}
