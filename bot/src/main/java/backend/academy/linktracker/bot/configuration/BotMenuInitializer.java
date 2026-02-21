package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.context.BotContext;
import backend.academy.linktracker.bot.handler.command.CommandHandler;
import backend.academy.linktracker.bot.handler.command.CommandHandlerRegistry;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import java.util.ArrayList;
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

    private final BotContext botContext;
    private final CommandHandlerRegistry commandHandlerRegistry;

    @Override
    public void run(ApplicationArguments args) {
        List<BotCommand> commands = new ArrayList<>();
        for (CommandHandler handler : commandHandlerRegistry.getAllHandlers()) {
            commands.add(new BotCommand(handler.getCommand(), handler.getDescription()));
        }

        botContext
                .bot()
                .execute(
                        new SetMyCommands(commands.toArray(new BotCommand[0])),
                        new Callback<SetMyCommands, BaseResponse>() {
                            @Override
                            public void onResponse(SetMyCommands request, BaseResponse response) {
                                if (!response.isOk()) {
                                    log.warn("Не удалось установить команды: {}", response.description());
                                } else {
                                    log.info("Команды успешно установлены.");
                                }
                            }

                            @Override
                            public void onFailure(SetMyCommands request, java.io.IOException e) {
                                log.error("Ошибка при установке команд", e);
                            }
                        });
    }
}
