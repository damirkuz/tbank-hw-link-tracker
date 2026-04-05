package backend.academy.linktracker.bot.update.handler.callback;

import backend.academy.linktracker.bot.update.handler.Handler;

public interface CallbackHandler extends Handler {
    boolean supports(String callbackData);
}
