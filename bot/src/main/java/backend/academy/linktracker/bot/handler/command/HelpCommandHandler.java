package backend.academy.linktracker.bot.handler.command;

import backend.academy.linktracker.bot.properties.message.MessageProperties;
import backend.academy.linktracker.bot.service.BotOperations;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HelpCommandHandler implements CommandHandler {

    private final MessageProperties messageProperties;
    private final BotOperations botOperations;

    @Override
    public void handle(Update update) {
        long chatId = update.message().chat().id();
        botOperations.sendMessage(chatId, messageProperties.helpCommand().answer());
    }

    @Override
    public String getCommand() {
        return messageProperties.helpCommand().command();
    }

    @Override
    public String getDescription() {
        return messageProperties.helpCommand().description();
    }

    @Override
    public boolean isCancelStateCommand() {
        return false;
    }
}
