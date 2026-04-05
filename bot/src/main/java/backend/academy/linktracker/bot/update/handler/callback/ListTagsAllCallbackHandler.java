package backend.academy.linktracker.bot.update.handler.callback;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.service.LinkListService;
import backend.academy.linktracker.bot.service.keyboard.TagKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListTagsAllCallbackHandler implements CallbackHandler {

    private final TagKeyboardFactory tagKeyboardFactory;
    private final TelegramBot bot;
    private final LinkListService linkListService;
    private final BotProperties botProperties;

    @Override
    public boolean supports(String callbackData) {
        return botProperties.tagKeyboard().callbackShowAll().equals(callbackData);
    }

    @Override
    public void handle(Update update, UpdateContext context) {
        long chatId = context.chatId();
        int messageId = update.callbackQuery().maybeInaccessibleMessage().messageId();

        Set<String> allTags = linkListService.collectTags(chatId);
        bot.execute(
                new EditMessageReplyMarkup(chatId, messageId).replyMarkup(tagKeyboardFactory.tagFilterAll(allTags)));
    }
}
