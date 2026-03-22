package backend.academy.linktracker.bot.update.router;

import backend.academy.linktracker.bot.config.properties.TelegramProperties;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.update.handler.UnknownUpdateHandler;
import backend.academy.linktracker.bot.update.handler.command.CommandHandlerRegistry;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandRouter implements Router {

    private final CommandHandlerRegistry commandHandlerRegistry;
    private final TelegramProperties telegramProperties;
    private final UnknownUpdateHandler unknownUpdateHandler;

    @Override
    public void route(Update update, UpdateContext updateContext) {
        updateContext
                .commandIdentifierForBot(telegramProperties.username())
                .flatMap(commandHandlerRegistry::findByCommandText)
                .ifPresentOrElse(
                        handler -> handler.handle(update, updateContext),
                        () -> unknownUpdateHandler.handle(update, updateContext));
    }
}
