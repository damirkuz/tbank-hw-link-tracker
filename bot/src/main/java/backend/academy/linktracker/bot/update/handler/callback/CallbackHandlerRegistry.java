package backend.academy.linktracker.bot.update.handler.callback;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CallbackHandlerRegistry {

    private final List<CallbackHandler> handlers;

    public Optional<CallbackHandler> findHandler(String callbackData) {
        return handlers.stream().filter(h -> h.supports(callbackData)).findFirst();
    }
}
