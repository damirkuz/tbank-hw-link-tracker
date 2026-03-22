package backend.academy.linktracker.bot.update.handler.global;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.config.properties.CommandMessage;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Update;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelpGlobalActionHandler implements GlobalActionHandler {

    private final BotOperations botOperations;
    private final BotTextService botTextService;
    private final BotProperties botProperties;

    @Override
    public boolean supports(UpdateContext context) {
        return context.isCallbackOrCommandAction("help");
    }

    @Override
    public void handle(Update update, UpdateContext context) {
        if (context.chatId() == null) {
            return;
        }

        botOperations.sendMessage(context.chatId(), getCommandsWithDescriptions());
    }

    private String getCommandsWithDescriptions() {
        Map<String, @Valid CommandMessage> messages = botProperties.messages();

        StringBuilder answer = new StringBuilder(botTextService.get("bot.common.help"));

        for (@Valid CommandMessage value : messages.values()) {
            answer.append("\n").append(value.command()).append(" - ").append(value.description());
        }
        return answer.toString();
    }
}
