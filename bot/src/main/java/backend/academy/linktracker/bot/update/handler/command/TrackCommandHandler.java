package backend.academy.linktracker.bot.update.handler.command;

import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("/track")
@RequiredArgsConstructor
public class TrackCommandHandler implements CommandHandler {
    private final BotTextService botTextService;
    private final StateStorage stateStorage;
    private final BotOperations botOperations;

    @Override
    public boolean isCancelStateCommand() {
        return true;
    }

    @Override
    public void handle(Update update) {
        long userId = update.message().from().id();

        stateStorage.updateState(userId, UserState.TRACK_WAIT_LINK);

        String answer = botTextService.get("bot.track.ask-link");

        botOperations.sendMessage(userId, answer);
    }
}
