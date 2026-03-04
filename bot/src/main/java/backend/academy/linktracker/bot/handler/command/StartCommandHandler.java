package backend.academy.linktracker.bot.handler.command;

import backend.academy.linktracker.bot.properties.message.MessageProperties;
import backend.academy.linktracker.bot.service.BotOperations;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StartCommandHandler implements CommandHandler {

    private final MessageProperties messageProperties;
    private final BotOperations botOperations;

    @Override
    public void handle(Update update) {
        long chatId = update.message().chat().id();
        botOperations.sendMessage(chatId, messageProperties.startCommand().answer());
    }

    @Override
    public String getCommand() {
        return messageProperties.startCommand().command();
    }

    @Override
    public String getDescription() {
        return messageProperties.startCommand().description();
    }

    @Override
    public boolean isCancelStateCommand() {
        return true;
    }
}
