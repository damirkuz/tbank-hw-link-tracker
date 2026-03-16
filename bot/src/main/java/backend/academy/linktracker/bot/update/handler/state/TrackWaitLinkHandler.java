package backend.academy.linktracker.bot.update.handler.state;

import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.validator.link.LinkValidationResult;
import backend.academy.linktracker.bot.validator.link.LinkValidationService;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackWaitLinkHandler implements StateHandler {

    private final LinkValidationService linkValidationService;
    private final BotTextService botTextService;
    private final BotOperations botOperations;
    private final StateStorage stateStorage;

    @Override
    public UserState getHandledState() {
        return UserState.TRACK_WAIT_LINK;
    }

    @Override
    public void handle(Update update) {

        long userId = update.message().from().id();
        long chatId = update.message().chat().id();

        String rawLink = update.message().text().trim();

        LinkValidationResult linkValidationResult = linkValidationService.validate(rawLink);
        String answer;
        if (linkValidationResult.valid()) {
            UserSession userSession = stateStorage.getUserSession(userId);
            userSession.setState(UserState.TRACK_WAIT_TAGS);
            userSession.setTrackLink(rawLink);

            answer = botTextService.get("bot.track.ask-tags");
        } else {
            // не меняем состояние, снова ждём ссылку
            answer = botTextService.get(
                    "bot.track.invalid-link", linkValidationResult.code(), linkValidationResult.message());
        }

        botOperations.sendMessage(chatId, answer);
    }
}
