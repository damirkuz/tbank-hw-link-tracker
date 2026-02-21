package backend.academy.linktracker.bot.handler.command;

import backend.academy.linktracker.bot.handler.Handler;

public interface CommandHandler extends Handler {
    String getCommand();

    String getDescription();

    boolean isCancelStateCommand();
}
