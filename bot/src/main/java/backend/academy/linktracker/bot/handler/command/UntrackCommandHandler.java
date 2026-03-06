package backend.academy.linktracker.bot.handler.command;

import com.pengrad.telegrambot.model.Update;

public class UntrackCommandHandler implements CommandHandler {
    @Override
    public boolean isCancelStateCommand() {
        return false;
    }

    @Override
    public void handle(Update update) {

    }
}
