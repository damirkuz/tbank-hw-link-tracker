package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.handler.command.CommandHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HandlerConfiguration {

    @Bean
    public Map<String, CommandHandler> commandHandlersMap(ObjectProvider<List<CommandHandler>> handlersProvider) {
        List<CommandHandler> handlers = handlersProvider.getIfAvailable(ArrayList::new);

        Map<String, CommandHandler> result = new ConcurrentHashMap<>();

        for (CommandHandler handler: handlers) {
            result.put(handler.getCommand(), handler);
        }
        return result;
    }
}
