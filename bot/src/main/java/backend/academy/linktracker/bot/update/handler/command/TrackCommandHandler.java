package backend.academy.linktracker.bot.update.handler.command;

import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("/track")
@RequiredArgsConstructor
public class TrackCommandHandler implements CommandHandler {
    private final BotTextService botTextService;
    private final StateStorage stateStorage;
    private final BotOperations botOperations;
    private final ReplyKeyboardFactory replyKeyboardFactory;

    @Override
    public void handle(Update update, UpdateContext updateContext) {
        stateStorage.updateState(updateContext.requireUserChatKey(), UserState.TRACK_WAIT_LINK);

        String answer = botTextService.get("bot.track.ask-link");

        botOperations.sendMessage(updateContext.chatId(), answer, replyKeyboardFactory.cancelOnly());
    }
}
