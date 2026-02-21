package backend.academy.linktracker.bot.router;

import backend.academy.linktracker.bot.handler.command.CommandHandlerRegistry;
import backend.academy.linktracker.bot.util.StringParser;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandRouter implements Router {

    private final CommandHandlerRegistry commandHandlers;
    private final StringParser parser;


    @Override
    public void route(Update update) {
        String commandParsed = parser.parseCommand(update.message().text());
        commandHandlers.getHandler(commandParsed).handle(update);
    }
}
