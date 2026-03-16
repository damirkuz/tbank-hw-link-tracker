package backend.academy.linktracker.bot.update.router;

import backend.academy.linktracker.bot.update.handler.idle.IdleHandler;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdleRouter implements Router {

    private final List<IdleHandler> handlers;

    @Override
    public void route(Update update) {
        for (IdleHandler handler : handlers) {
            if (handler.canHandle(update)) {
                handler.handle(update);
                break;
            }
        }
    }
}
