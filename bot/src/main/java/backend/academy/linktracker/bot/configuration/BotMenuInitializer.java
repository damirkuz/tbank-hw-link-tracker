package backend.academy.linktracker.bot.configuration;


import backend.academy.linktracker.bot.handler.command.registry.CommandHandlerRegistry;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BotMenuInitializer implements ApplicationRunner {

    private final TelegramBot bot;
    private final CommandHandlerRegistry commandHandlerRegistry;

    @Override
    public void run(ApplicationArguments args) {
        List<BotCommand> commands = commandHandlerRegistry.getCommands().stream()
            .map(c -> new BotCommand(c.command(), c.description()))
            .toList();

        try {
            BaseResponse response = bot.execute(new SetMyCommands(commands.toArray(new BotCommand[0])));
            if (!response.isOk()) {
                log.atError()
                        .addKeyValue("error_code", response.errorCode())
                        .addKeyValue("description", response.description())
                        .log("Не удалось установить команды меню");
            } else {
                log.atInfo().addKeyValue("commands_count", commands.size()).log("Меню команд успешно зарегистрировано");
            }
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("error_type", e.getClass().getSimpleName())
                    .log("Ошибка сети при попытке зарегистрировать меню команд", e);
        }
    }
}
