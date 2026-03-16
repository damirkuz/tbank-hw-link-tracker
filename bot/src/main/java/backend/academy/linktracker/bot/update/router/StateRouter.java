package backend.academy.linktracker.bot.update.router;

import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.update.handler.state.StateHandlerRegistry;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StateRouter implements Router {

    private final StateStorage stateStorage;

    private final StateHandlerRegistry handlerRegistry;

    @Override
    public void route(Update update) {
        UserState userState =
                stateStorage.getUserSession(update.message().from().id()).getState();

        handlerRegistry.getHandler(userState).handle(update);
    }
}
