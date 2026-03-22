package backend.academy.linktracker.bot.update.handler.command;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.config.properties.CommandMessage;
import backend.academy.linktracker.bot.model.MyBotCommand;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandHandlerRegistry {

    private final Map<String, CommandHandler> handlers;
    private final BotProperties botProperties;

    public Optional<CommandHandler> findByCommandText(String commandText) {
        return botProperties.messages().values().stream()
                .map(CommandMessage::command)
                .filter(command -> command.equals(commandText))
                .map(handlers::get)
                .findFirst();
    }

    public List<MyBotCommand> getCommands() {
        return botProperties.messages().values().stream()
                .map(commandMessage -> new MyBotCommand(
                        commandMessage.command(), commandMessage.description(), handlers.get(commandMessage.command())))
                .toList();
    }
}
