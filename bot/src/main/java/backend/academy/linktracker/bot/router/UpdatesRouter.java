package backend.academy.linktracker.bot.router;


import backend.academy.linktracker.bot.handler.AbstractHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class UpdatesRouter {
    private final TelegramBot bot;

    private final List<AbstractHandler> abstractHandlers;

    private void goToHandlers(Update update) {
        for (AbstractHandler abstractHandler : abstractHandlers) {
            if (abstractHandler.canHandle(update)) {
                abstractHandler.handle(update);
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

        BotCommand[] commands = new BotCommand[] {
            new BotCommand("start", "Запустить бота"),
            new BotCommand("help", "Показать помощь")
        };

        BaseResponse response = bot.execute(new SetMyCommands(commands));
        if (!response.isOk()) {
            log.atDebug().log("Не удалось установить команды");
        }
    }
}
