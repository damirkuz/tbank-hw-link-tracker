package backend.academy.linktracker.bot.configuration;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

@Slf4j
@RequiredArgsConstructor
public class BotMenuInitializer implements ApplicationRunner {

    private final TelegramBot bot;

    @Override
    public void run(ApplicationArguments args) {
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
