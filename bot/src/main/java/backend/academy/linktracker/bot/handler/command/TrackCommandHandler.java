package backend.academy.linktracker.bot.handler.command;

import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackCommandHandler implements CommandHandler {
    private final BotTextService botTextService;
    private final StateRepository stateRepository;
    private final BotOperations botOperations;


    @Override
    public boolean isCancelStateCommand() {
        return true;
    }

    @Override
    public void handle(Update update) {
        long userId = update.message().chat().id();

        stateRepository.setUserState(userId, UserState.TRACK_WAIT_LINK);

        String answer = botTextService.get("bot.track.ask-link");

        botOperations.sendMessage(userId, answer);
    }
}
