package backend.academy.linktracker.bot.update.handler.callback;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.LinkListService;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListTagCallbackHandler implements CallbackHandler {

    private final BotOperations botOperations;
    private final LinkListService linkListService;
    private final ReplyKeyboardFactory replyKeyboardFactory;
    private final BotProperties botProperties;

    @Override
    public boolean supports(String callbackData) {
        return callbackData != null
                && callbackData.startsWith(botProperties.tagKeyboard().callbackPrefix());
    }

    @Override
    public void handle(Update update, UpdateContext context) {
        long chatId = context.chatId();
        String tag = context.callbackData()
                .substring(botProperties.tagKeyboard().callbackPrefix().length());
        String answer = linkListService.buildListMessage(chatId, tag);
        botOperations.sendMessage(chatId, answer, replyKeyboardFactory.mainMenu());
    }
}
