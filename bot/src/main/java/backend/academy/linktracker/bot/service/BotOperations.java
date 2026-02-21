package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.context.BotContext;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class BotOperations {

    private final BotContext botContext;

    public void sendMessage(long chatId, String message) {
        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue("message_length", message.length())
                .log("Отправка ответного сообщения");

        SendResponse response = botContext.bot().execute(new SendMessage(chatId, message));

        if (!response.isOk()) {
            log.atError()
                    .addKeyValue("chat_id", chatId)
                    .addKeyValue("error_code", response.errorCode())
                    .addKeyValue("description", response.description())
                    .log("Не удалось отправить сообщение пользователю");
        }
    }
}
