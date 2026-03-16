package backend.academy.linktracker.bot.update.handler.state;

import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.update.handler.Handler;

public interface StateHandler extends Handler {

    UserState getHandledState();
}
