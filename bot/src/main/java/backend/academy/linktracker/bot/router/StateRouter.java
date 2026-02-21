package backend.academy.linktracker.bot.router;

import backend.academy.linktracker.bot.handler.state.StateHandlerRegistry;
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
                stateRepository.getUserState(update.message().chat().id());

        handlerRegistry.getHandler(userState).handle(update);
    }
}
