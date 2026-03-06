package backend.academy.linktracker.bot.handler.state;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.bot.client.request.AddLinkRequest;
import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackWaitTagsHandler implements StateHandler {
    private final ScrapperClient scrapperClient;
    private final StateRepository stateRepository;
    private final BotTextService botTextService;
    private final BotOperations botOperations;


    @Override
    public UserState getHandledState() {
        return UserState.TRACK_WAIT_TAGS;
    }

    @Override
    public void handle(Update update) {

        long userId = update.message().from().id();
        long chatId = update.message().chat().id();

        String[] tags = update.message().text().trim().split(",");

        UserSession userSession = stateRepository.getUserSession(userId);

        AddLinkRequest addLinkRequest = new AddLinkRequest(userSession.getTrackLink(), tags, null);

        String answer;

        try {
            scrapperClient.addLink(chatId, addLinkRequest);
            answer = botTextService.get("bot.track.success");
        } catch (LinkAlreadyTrackedException e) {
            answer = botTextService.get("bot.track.link-already-add");
        }

        userSession.setTrackLink(null);
        userSession.setState(UserState.IDLE);
        botOperations.sendMessage(chatId, answer);
    }
}
