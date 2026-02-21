package backend.academy.linktracker.bot.dispatcher;


import backend.academy.linktracker.bot.handler.UnknownUpdateHandler;
import backend.academy.linktracker.bot.handler.command.CommandHandler;
import backend.academy.linktracker.bot.handler.command.CommandHandlerRegistry;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.router.CommandRouter;
import backend.academy.linktracker.bot.router.MainRouter;
import backend.academy.linktracker.bot.router.Router;
import backend.academy.linktracker.bot.router.StateRouter;
import backend.academy.linktracker.bot.util.StringParser;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateDispatcher {
    private final TelegramBot bot;

    private final CommandRouter commandRouter;
    private final MainRouter mainRouter;
    private final StateRouter stateRouter;

    private final StateRepository stateRepository;

    private final List<Router> routers;

    private final CommandHandlerRegistry commandHandlers;

    private final StringParser parser;

    private final UnknownUpdateHandler unknownUpdateHandler;

    private void goToRouters(Update update) {
        long chatId = update.message().chat().id();
        String text = update.message().text() != null ? update.message().text() : "";
        String command = text.startsWith("/") ? parser.parseCommand(text) : null;
        UserState currentState = stateRepository.getUserState(chatId);
        CommandHandler handler = command != null ? commandHandlers.getHandler(command) : null;

        if (handler != null && handler.isCancelStateCommand()) {
            commandRouter.route(update);
            stateRepository.setUserState(chatId, UserState.MAIN);
        } else if (currentState != UserState.MAIN) {
            mainRouter.route(update);
        } else if (handler != null) {
            commandRouter.route(update);
        } else {
            unknownUpdateHandler.handle(update);
        }
    }


    @PostConstruct
    public void init() {
        bot.setUpdatesListener(
            updates -> {
                for (Update update : updates) {
                    goToRouters(update);
                }

                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        );
    }
}
