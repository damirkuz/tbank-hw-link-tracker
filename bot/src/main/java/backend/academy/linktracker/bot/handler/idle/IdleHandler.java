package backend.academy.linktracker.bot.handler.idle;

import backend.academy.linktracker.bot.handler.Handler;
import com.pengrad.telegrambot.model.Update;

public interface IdleHandler extends Handler {
    boolean canHandle(Update update);
}
