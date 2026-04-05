package backend.academy.linktracker.bot.update.handler.global;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.model.InterruptionPolicy;
import backend.academy.linktracker.bot.model.UserChatKey;
import backend.academy.linktracker.bot.model.UserSession;
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
public class CancelGlobalActionHandler implements GlobalActionHandler {

    private final BotOperations botOperations;
    private final BotTextService botTextService;
    private final StateStorage stateStorage;
    private final ReplyKeyboardFactory replyKeyboardFactory;
    private final BotProperties botProperties;

    @Override
    public boolean supports(UpdateContext context) {
        return context.isCallbackOrCommandAction("cancel")
                || botProperties.isAliasForCommand("/cancel", context.messageText());
    }

    @Override
    public void handle(Update update, UpdateContext context) {
        if (context.chatId() == null || context.userId() == null) {
            return;
        }

        UserChatKey userChatKey = context.requireUserChatKey();
        UserSession session = stateStorage.getUserSession(userChatKey);
        if (session == null || session.getState() == UserState.IDLE) {
            botOperations.sendMessage(context.chatId(), botTextService.get("bot.common.nothing-to-cancel"));
            return;
        }

        if (session.getInterruptionPolicy() == InterruptionPolicy.BLOCK_ALL) {
            botOperations.sendMessage(context.chatId(), botTextService.get("bot.common.cancel-blocked"));
            return;
        }

        stateStorage.clearState(userChatKey);
        botOperations.sendMessage(
                context.chatId(), botTextService.get("bot.common.cancelled"), replyKeyboardFactory.mainMenu());
    }
}
