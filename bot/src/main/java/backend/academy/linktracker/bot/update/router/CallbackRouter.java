package backend.academy.linktracker.bot.update.router;

import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CallbackRouter implements Router {

    public void route(Update update, UpdateContext updateContext) {
        // пока не нужны были колбэки
    }
}
