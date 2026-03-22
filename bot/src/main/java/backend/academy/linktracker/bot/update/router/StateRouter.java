package backend.academy.linktracker.bot.update.router;

import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.update.handler.UnknownUpdateHandler;
import backend.academy.linktracker.bot.update.handler.state.StateHandlerRegistry;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StateRouter implements Router {

    private final StateStorage stateStorage;

    private final StateHandlerRegistry handlerRegistry;

    private final UnknownUpdateHandler unknownUpdateHandler;

    @Override
    public void route(Update update, UpdateContext updateContext) {
        UserState userState =
                stateStorage.getUserSession(updateContext.userId()).getState();

        handlerRegistry
                .getHandler(userState)
                .ifPresentOrElse(
                        handler -> handler.handle(update, updateContext),
                        () -> unknownUpdateHandler.handle(update, updateContext));
    }
}
