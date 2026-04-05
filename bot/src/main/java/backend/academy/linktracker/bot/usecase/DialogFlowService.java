package backend.academy.linktracker.bot.usecase;

import backend.academy.linktracker.bot.model.InterruptionPolicy;
import backend.academy.linktracker.bot.model.UserChatKey;
import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DialogFlowService {

    private final StateStorage stateStorage;
    private final BotTextService botTextService;
    private final BotOperations botOperations;
    private final ReplyKeyboardFactory replyKeyboardFactory;

    public void startTrackFlow(UpdateContext context) {
        stateStorage.updateState(context.requireUserChatKey(), UserState.TRACK_WAIT_LINK);
        botOperations.sendMessage(
                context.chatId(), botTextService.get("bot.track.ask-link"), replyKeyboardFactory.cancelOnly());
    }

    public void startUntrackFlow(UpdateContext context) {
        stateStorage.updateState(context.requireUserChatKey(), UserState.UNTRACK_WAIT_LINK);
        botOperations.sendMessage(
                context.chatId(), botTextService.get("bot.untrack.ask-link"), replyKeyboardFactory.cancelOnly());
    }

    public void startConversation(UpdateContext context) {
        stateStorage.clearState(context.requireUserChatKey());
        botOperations.sendMessage(
                context.chatId(), botTextService.get("bot.common.start"), replyKeyboardFactory.mainMenu());
    }

    public void cancelConversation(UpdateContext context) {
        UserChatKey userChatKey = context.requireUserChatKey();
        UserSession session = stateStorage.getUserSession(userChatKey);

        if (session.getState() == UserState.IDLE) {
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
