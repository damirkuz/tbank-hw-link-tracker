package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.context.BotContext;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BotOperations {

    private final BotContext botContext;

    public void sendMessage(long chatId, String message) {
        botContext.bot().execute(new SendMessage(chatId, message));
    }
}
