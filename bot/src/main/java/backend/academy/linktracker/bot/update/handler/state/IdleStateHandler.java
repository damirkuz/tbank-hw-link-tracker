package backend.academy.linktracker.bot.update.handler.state;

import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.update.handler.UnknownUpdateHandler;
import backend.academy.linktracker.bot.update.handler.command.CommandHandlerRegistry;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdleStateHandler implements StateHandler {

    private final CommandHandlerRegistry commandHandlerRegistry;
    private final UnknownUpdateHandler unknownUpdateHandler;

    @Override
    public UserState getHandledState() {
        return UserState.IDLE;
    }

    @Override
    public void handle(Update update, UpdateContext context) {
        commandHandlerRegistry
                .findByAlias(context.messageText())
                .ifPresentOrElse(
                        handler -> handler.handle(update, context), () -> unknownUpdateHandler.handle(update, context));
    }
}
