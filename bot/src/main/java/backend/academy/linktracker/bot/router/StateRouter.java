package backend.academy.linktracker.bot.router;

import backend.academy.linktracker.bot.handler.registry.StateHandlerRegistry;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.repository.StateRepository;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StateRouter implements Router {

    private final StateRepository stateRepository;

    private final StateHandlerRegistry handlerRegistry;

    @Override
    public void route(Update update) {
        UserState userState =
                stateRepository.getUserSession(update.message().from().id()).getState();

        handlerRegistry.getHandler(userState).handle(update);
    }
}
