package backend.academy.linktracker.bot.service.keyboard;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplyKeyboardFactory {

    private final BotProperties botProperties;

    public ReplyKeyboardMarkup mainMenu() {
        return new ReplyKeyboardMarkup(new KeyboardButton[][] {
                    {btn("/list"), btn("/track")},
                    {btn("/untrack"), btn("/help")}
                })
                .resizeKeyboard(true);
    }

    public ReplyKeyboardMarkup cancelOnly() {
        return new ReplyKeyboardMarkup(new KeyboardButton[][] {{btn("/cancel")}})
                .resizeKeyboard(true)
                .oneTimeKeyboard(true);
    }

    public ReplyKeyboardRemove remove() {
        return new ReplyKeyboardRemove();
    }

    private KeyboardButton btn(String command) {
        return new KeyboardButton(botProperties.firstAliasOrCommand(command));
    }
}
