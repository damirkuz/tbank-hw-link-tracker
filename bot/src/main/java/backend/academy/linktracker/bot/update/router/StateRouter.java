package backend.academy.linktracker.bot.update.router;

import backend.academy.linktracker.bot.model.UserChatKey;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.update.handler.UnknownUpdateHandler;
import backend.academy.linktracker.bot.update.handler.state.StateHandlerRegistry;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateRouter implements Router {

    private final StateStorage stateStorage;
    private final StateHandlerRegistry handlerRegistry;
    private final UnknownUpdateHandler unknownUpdateHandler;

    @Override
    public void route(Update update, UpdateContext updateContext) {
        Long userId = updateContext.userId();
        if (userId == null) {
            log.warn("Пропускаем роутинг, тк userId равен null");
            unknownUpdateHandler.handle(update, updateContext);
            return;
        }
        Long chatId = updateContext.chatId();
        if (chatId == null) {
            log.warn("Пропускаем роутинг, тк chatId равен null");
            unknownUpdateHandler.handle(update, updateContext);
            return;
        }

        UserChatKey userChatKey = updateContext.requireUserChatKey();
        UserState userState = stateStorage.getUserSession(userChatKey).getState();

        handlerRegistry
                .getHandler(userState)
                .ifPresentOrElse(
                        handler -> handler.handle(update, updateContext),
                        () -> unknownUpdateHandler.handle(update, updateContext));
    }
}
