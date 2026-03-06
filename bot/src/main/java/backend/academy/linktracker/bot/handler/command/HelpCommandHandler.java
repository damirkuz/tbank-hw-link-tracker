package backend.academy.linktracker.bot.handler.command;


import backend.academy.linktracker.bot.properties.BotProperties;
import backend.academy.linktracker.bot.properties.message.CommandMessage;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import com.pengrad.telegrambot.model.Update;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("/help")
@RequiredArgsConstructor
public class HelpCommandHandler implements CommandHandler {

    private final BotTextService botTextService;
    private final BotOperations botOperations;
    private final BotProperties botProperties;


    @Override
    public void handle(Update update) {
        long chatId = update.message().chat().id();

        botOperations.sendMessage(chatId, getCommandsWithDescriptions());
    }

    private String getCommandsWithDescriptions() {
        Map<String, @Valid CommandMessage> messages = botProperties.messages();

        StringBuilder answer = new StringBuilder(botTextService.get("bot.common.help"));

        for (@Valid CommandMessage value : messages.values()) {
            answer.append("\n").append(value.command()).append(" - ").append(value.description());
        }
        return answer.toString();
    }


    @Override
    public boolean isCancelStateCommand() {
        return false;
    }
}
