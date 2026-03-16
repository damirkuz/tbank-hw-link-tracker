package backend.academy.linktracker.bot.update.router;

import backend.academy.linktracker.bot.update.handler.command.CommandHandler;
import backend.academy.linktracker.bot.update.handler.command.CommandHandlerRegistry;
import backend.academy.linktracker.bot.util.StringParser;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandRouter implements Router {

    private final CommandHandlerRegistry commandHandlerRegistry;
    private final StringParser parser;

    @Override
    public void route(Update update) {
        String commandParsed = parser.parseCommand(update.message().text());
        commandHandlerRegistry.findByCommandText(commandParsed).ifPresent(handler -> handler.handle(update));
    }

    public void route(Update update, CommandHandler handler) {
        handler.handle(update);
    }
}
