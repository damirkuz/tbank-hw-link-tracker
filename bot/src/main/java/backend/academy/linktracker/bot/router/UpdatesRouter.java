package backend.academy.linktracker.bot.router;


import backend.academy.linktracker.bot.handler.Handler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class UpdatesRouter {
    private final TelegramBot bot;

    private final List<Handler> handlers;

//    public void addHandler(Handler handler) {
//        if (handlers == null) {
//            handlers = new ArrayList<>();
//        }
//        handlers.add(handler);
//    }

    private void goToHandlers(Update update) {
        for (Handler handler : handlers) {
            if (handler.canHandle(update)) {
                handler.handle(update);
                break;
            }
        }
    }


    @PostConstruct
    public void init() {
        bot.setUpdatesListener(
            updates -> {
                for (Update update : updates) {
                    goToHandlers(update);
                }

                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        );
    }
}
