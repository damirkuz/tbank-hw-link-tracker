package backend.academy.linktracker.bot.handler.state;

import backend.academy.linktracker.bot.handler.Handler;
import backend.academy.linktracker.bot.model.UserState;

public interface StateHandler extends Handler {

    UserState getHandledState();
}
