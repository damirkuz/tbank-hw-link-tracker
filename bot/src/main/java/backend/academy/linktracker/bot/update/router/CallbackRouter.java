package backend.academy.linktracker.bot.update.router;

import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.update.handler.UnknownUpdateHandler;
import backend.academy.linktracker.bot.update.handler.callback.CallbackHandlerRegistry;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CallbackRouter implements Router {

    private final CallbackHandlerRegistry callbackHandlerRegistry;
    private final UnknownUpdateHandler unknownUpdateHandler;

    @Override
    public void route(Update update, UpdateContext context) {
        callbackHandlerRegistry
                .findHandler(context.callbackData())
                .ifPresentOrElse(
                        handler -> handler.handle(update, context), () -> unknownUpdateHandler.handle(update, context));
    }
}
