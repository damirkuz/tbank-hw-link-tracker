package backend.academy.linktracker.bot.update.handler.global;

import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.update.handler.Handler;

public interface GlobalActionHandler extends Handler {
    boolean supports(UpdateContext context);
}
