package backend.academy.linktracker.bot.dispatcher;

import backend.academy.linktracker.bot.handler.command.CommandHandler;
import backend.academy.linktracker.bot.handler.command.CommandHandlerRegistry;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.router.CommandRouter;
import backend.academy.linktracker.bot.router.IdleRouter;
import backend.academy.linktracker.bot.router.StateRouter;
import backend.academy.linktracker.bot.util.StringParser;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateDispatcher {
    private final TelegramBot bot;

    private final CommandRouter commandRouter;
    private final IdleRouter idleRouter;
    private final StateRouter stateRouter;

    private final StateRepository stateRepository;

    private final CommandHandlerRegistry commandHandlers;

    private final StringParser parser;

    public void dispatch(Update update) {
        if (update.message() == null || update.message().chat() == null) {
            log.atDebug().log("Получен Update без message или chat. Пропускаем.");
            return;
        }

        long chatId = update.message().chat().id();
        String text = update.message().text() != null ? update.message().text() : "";
        String command = text.startsWith("/") ? parser.parseCommand(text) : null;
        UserState currentState = stateRepository.getUserState(chatId);
        CommandHandler handler = command != null ? commandHandlers.getHandler(command) : null;

        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue(
                        "username",
                        update.message().from() != null
                                ? update.message().from().username()
                                : "unknown")
                .addKeyValue("text", text)
                .addKeyValue("current_state", currentState)
                .addKeyValue("is_command", command != null)
                .log("Обработка входящего обновления");

        if (handler != null && handler.isCancelStateCommand()) {
            commandRouter.route(update, command);
            stateRepository.setUserState(chatId, UserState.IDLE);
        } else if (currentState != UserState.IDLE) {
            stateRouter.route(update);
        } else if (handler != null) {
            commandRouter.route(update, command);
        } else {
            idleRouter.route(update);
        }
    }

    @PostConstruct
    public void init() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                try {
                    dispatch(update);
                } catch (Exception e) {
                    log.atError()
                            .addKeyValue("update_id", update.updateId())
                            .log("Ошибка во время обработки обновления", e);
                }
            }

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
