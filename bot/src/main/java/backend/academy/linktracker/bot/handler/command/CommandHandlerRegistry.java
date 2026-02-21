package backend.academy.linktracker.bot.handler.command;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CommandHandlerRegistry {

    private final Map<String, CommandHandler> map;

    public CommandHandlerRegistry(List<CommandHandler> handlers) {
        Map<String, CommandHandler> result = new HashMap<>();
        for (CommandHandler handler : handlers) {
            result.put(handler.getCommand(), handler);
        }
        this.map = Collections.unmodifiableMap(result);
    }

    public CommandHandler getHandler(String command) {
        return map.get(command);
    }

    public Collection<CommandHandler> getAllHandlers() {
        return map.values();
    }
}
