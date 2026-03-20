package backend.academy.linktracker.bot.update.handler.state;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import com.pengrad.telegrambot.model.Update;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackWaitTagsHandler implements StateHandler {
    private final ScrapperGateway scrapperClient;
    private final StateStorage stateStorage;
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

        String text = update.message().text().trim().toLowerCase(Locale.ROOT);
        List<String> tags = new ArrayList<>();
        if (!text.equals(botTextService.get("bot.reject"))) {
            tags = List.of(text.split(","));
        }

        UserSession userSession = stateStorage.getUserSession(userId);

        CommonAddLinkRequest commonAddLinkRequest =
                new CommonAddLinkRequest(userSession.getTrackLink(), tags, new ArrayList<>());

        String answer;

        try {
            scrapperClient.addLink(chatId, commonAddLinkRequest);
            answer = botTextService.get("bot.track.success");
        } catch (LinkAlreadyTrackedException e) {
            answer = botTextService.get("bot.track.link-already-add");
        } catch (ChatNotFoundException e) {
            answer = botTextService.get("bot.track.chat-not-found");
        } catch (Exception e) {
            answer = botTextService.get("bot.common.unknown-error");
            log.atError().setCause(e).log(answer);
        }

        userSession.setTrackLink(null);
        userSession.setState(UserState.IDLE);
        stateStorage.save(userId, userSession);
        botOperations.sendMessage(chatId, answer);
    }
}
