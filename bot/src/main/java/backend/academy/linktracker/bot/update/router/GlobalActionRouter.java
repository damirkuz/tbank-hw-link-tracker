package backend.academy.linktracker.bot.update.router;

import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.update.handler.global.GlobalActionHandler;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GlobalActionRouter {

    private final List<GlobalActionHandler> handlers;

    public boolean tryHandle(Update update, UpdateContext context) {
        for (GlobalActionHandler handler : handlers) {
            if (handler.supports(context)) {
                handler.handle(update, context);
                return true;
            }
        }
        return false;
    }
}
