package backend.academy.linktracker.bot.update.handler.state;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.bot.model.UserChatKey;
import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import com.pengrad.telegrambot.model.Update;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final ReplyKeyboardFactory replyKeyboardFactory;

    @Override
    public UserState getHandledState() {
        return UserState.TRACK_WAIT_TAGS;
    }

    @Override
    public void handle(Update update, UpdateContext updateContext) {
        long userId = updateContext.userId();
        long chatId = updateContext.chatId();
        UserChatKey userChatKey = updateContext.requireUserChatKey();

        String text = update.message().text().trim().toLowerCase(Locale.ROOT);
        List<String> tags = new ArrayList<>();
        if (!text.equals(botTextService.get("bot.reject"))) {
            tags = Arrays.stream(text.split(","))
                    .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
                    .toList();
        }

        UserSession userSession = stateStorage.getUserSession(userChatKey);
        URI trackLink = userSession.getTrackLink();

        CommonAddLinkRequest request = new CommonAddLinkRequest(trackLink, tags, new ArrayList<>());

        String answer;

        try {
            scrapperClient.addLink(chatId, request);
            answer = botTextService.get("bot.track.success");
        } catch (LinkAlreadyTrackedException e) {
            answer = botTextService.get("bot.track.link-already-add");
        } catch (ChatNotFoundException e) {
            log.atWarn()
                    .addKeyValue("client", "scrapper")
                    .addKeyValue("operation", "addLink")
                    .addKeyValue("chat_id", chatId)
                    .addKeyValue("user_id", userId)
                    .addKeyValue("link", trackLink)
                    .addKeyValue("tags", tags)
                    .addKeyValue("exception", e.getClass().getSimpleName())
                    .log("Чат не найден в scrapper, выполняется регистрация и один повтор запроса");

            try {
                scrapperClient.registerChat(chatId);

                log.atInfo()
                        .addKeyValue("client", "scrapper")
                        .addKeyValue("operation", "registerChat")
                        .addKeyValue("chat_id", chatId)
                        .log("Чат зарегистрирован, повторяем addLink один раз");

                scrapperClient.addLink(chatId, request);
                answer = botTextService.get("bot.track.success");
            } catch (LinkAlreadyTrackedException ex) {
                answer = botTextService.get("bot.track.link-already-add");
            } catch (Exception ex) {
                answer = botTextService.get("bot.common.unknown-error");
                log.atError()
                        .setCause(ex)
                        .addKeyValue("client", "scrapper")
                        .addKeyValue("operation", "addLink.retry")
                        .addKeyValue("chat_id", chatId)
                        .addKeyValue("user_id", userId)
                        .addKeyValue("link", trackLink)
                        .addKeyValue("tags", tags)
                        .log("Ошибка при повторной попытке добавить ссылку после регистрации чата");
            }
        } catch (Exception e) {
            answer = botTextService.get("bot.common.unknown-error");
            log.atError()
                    .setCause(e)
                    .addKeyValue("client", "scrapper")
                    .addKeyValue("operation", "addLink")
                    .addKeyValue("chat_id", chatId)
                    .addKeyValue("user_id", userId)
                    .addKeyValue("link", trackLink)
                    .addKeyValue("tags", tags)
                    .log(answer);
        }

        userSession.setTrackLink(null);
        userSession.setState(UserState.IDLE);
        stateStorage.save(userChatKey, userSession);
        botOperations.sendMessage(chatId, answer, replyKeyboardFactory.mainMenu());
    }
}
