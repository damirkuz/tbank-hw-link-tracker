package backend.academy.linktracker.bot.update.context;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class UpdateContextExtractor {

    public UpdateContext extract(Update update) {
        Long chatId = null;
        Long userId = null;
        String messageText = null;
        String callbackData = null;

        if (update.message() != null) {
            if (update.message().chat() != null) {
                chatId = update.message().chat().id();
            }

            if (update.message().from() != null) {
                userId = update.message().from().id();
            }

            messageText = update.message().text();
        }

        CallbackQuery callbackQuery = update.callbackQuery();
        if (callbackQuery != null) {
            if (callbackQuery.from() != null) {
                userId = callbackQuery.from().id();
            }

            callbackData = callbackQuery.data();

            var maybeMessage = callbackQuery.maybeInaccessibleMessage();
            if (maybeMessage != null && maybeMessage.chat() != null) {
                chatId = maybeMessage.chat().id();
            }
        }

        return new UpdateContext(update.updateId(), chatId, userId, messageText, callbackData);
    }
}
