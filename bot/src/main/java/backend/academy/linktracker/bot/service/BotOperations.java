package backend.academy.linktracker.bot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class BotOperations {

    private final TelegramBot bot;

    private static final int TELEGRAM_MESSAGE_LIMIT = 4096;

    public void sendMessage(long chatId, String message) {
        if (message == null || message.isBlank()) {
            log.atWarn().addKeyValue("chat_id", chatId).log("Пустое сообщение, отправка пропущена");
            return;
        }

        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue("message_length", message.length())
                .log("Отправка ответного сообщения");

        for (String part : splitMessage(message, TELEGRAM_MESSAGE_LIMIT)) {
            SendResponse response = bot.execute(new SendMessage(chatId, part));

            if (!response.isOk()) {
                log.atError()
                        .addKeyValue("chat_id", chatId)
                        .addKeyValue("error_code", response.errorCode())
                        .addKeyValue("description", response.description())
                        .addKeyValue("message_part_length", part.length())
                        .log("Не удалось отправить сообщение пользователю");

                return; // не отправляем другие части после ошибки
            }
        }
    }

    public void sendMessage(long chatId, String message, Keyboard keyboard) {
        if (message == null || message.isBlank()) {
            log.atWarn().addKeyValue("chat_id", chatId).log("Пустое сообщение, отправка пропущена");
            return;
        }

        SendMessage request = new SendMessage(chatId, message).replyMarkup(keyboard);
        SendResponse response = bot.execute(request);

        if (!response.isOk()) {
            log.atError()
                    .addKeyValue("chat_id", chatId)
                    .addKeyValue("error_code", response.errorCode())
                    .addKeyValue("description", response.description())
                    .log("Не удалось отправить сообщение с клавиатурой");
        }
    }

    private List<String> splitMessage(String message, int maxLength) {
        List<String> parts = new ArrayList<>();

        int start = 0;
        while (start < message.length()) {
            int end = Math.min(start + maxLength, message.length());

            // Пытаемся резать по переводу строки
            if (end < message.length()) {
                int lastNewLine = message.lastIndexOf('\n', end);
                if (lastNewLine > start) {
                    end = lastNewLine;
                }
            }

            parts.add(message.substring(start, end));
            start = end;

            // пропускаем лишний перенос, если разрезали по \n
            if (start < message.length() && message.charAt(start) == '\n') {
                start++;
            }
        }

        return parts;
    }
}
