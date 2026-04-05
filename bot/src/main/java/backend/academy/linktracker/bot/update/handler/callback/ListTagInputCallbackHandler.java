package backend.academy.linktracker.bot.update.handler.callback;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListTagInputCallbackHandler implements CallbackHandler {

    private final StateStorage stateStorage;
    private final BotOperations botOperations;
    private final BotTextService botTextService;
    private final ReplyKeyboardFactory replyKeyboardFactory;
    private final BotProperties botProperties;

    @Override
    public boolean supports(String callbackData) {
        return botProperties.tagKeyboard().callbackInput().equals(callbackData);
    }

    @Override
    public void handle(Update update, UpdateContext context) {
        stateStorage.updateState(context.requireUserChatKey(), UserState.LIST_WAIT_TAG);
        botOperations.sendMessage(
                context.chatId(), botTextService.get("bot.list.ask-tag"), replyKeyboardFactory.cancelOnly());
    }
}
