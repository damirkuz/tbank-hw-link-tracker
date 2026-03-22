package backend.academy.linktracker.bot.update.handler.global;

import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Update;

public interface GlobalActionHandler {
    boolean supports(UpdateContext context);

    void handle(Update update, UpdateContext context);
}
