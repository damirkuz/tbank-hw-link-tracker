package backend.academy.linktracker.bot.usecase;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.bot.model.UserChatKey;
import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.validator.link.LinkValidationResult;
import backend.academy.linktracker.bot.validator.link.LinkValidationService;
import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackFlowService {

    private final LinkValidationService linkValidationService;
    private final ScrapperGateway scrapperGateway;
    private final StateStorage stateStorage;
    private final BotTextService botTextService;
    private final BotOperations botOperations;
    private final ReplyKeyboardFactory replyKeyboardFactory;

    public void handleLinkInput(UpdateContext updateContext, String rawLink) {
        long chatId = updateContext.chatId();
        UserChatKey userChatKey = updateContext.requireUserChatKey();

        LinkValidationResult validationResult = linkValidationService.validate(rawLink.trim());
        String answer;

        if (validationResult.valid()) {
            UserSession userSession = stateStorage.getUserSession(userChatKey);
            userSession.setState(UserState.TRACK_WAIT_TAGS);
            userSession.setTrackLink(URI.create(rawLink.trim()));
            stateStorage.save(userChatKey, userSession);

            answer = botTextService.get("bot.track.ask-tags");
        } else {
            answer = botTextService.get("bot.track.invalid-link");
        }

        botOperations.sendMessage(chatId, answer);
    }

    public void handleTagsInput(UpdateContext updateContext, String rawTags) {
        long userId = updateContext.userId();
        long chatId = updateContext.chatId();
        UserChatKey userChatKey = updateContext.requireUserChatKey();

        List<String> tags = parseTags(rawTags);
        UserSession userSession = stateStorage.getUserSession(userChatKey);
        URI trackLink = userSession.getTrackLink();
        CommonAddLinkRequest request = new CommonAddLinkRequest(trackLink, tags, List.of());

        String answer = addLinkWithRecovery(chatId, userId, trackLink, tags, request);

        userSession.setTrackLink(null);
        userSession.setState(UserState.IDLE);
        stateStorage.save(userChatKey, userSession);
        botOperations.sendMessage(chatId, answer, replyKeyboardFactory.mainMenu());
    }

    private List<String> parseTags(String rawTags) {
        String normalizedText = rawTags.trim().toLowerCase(Locale.ROOT);
        if (normalizedText.equals(botTextService.get("bot.reject"))) {
            return List.of();
        }

        return Arrays.stream(normalizedText.split(","))
                .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
                .filter(tag -> !tag.isBlank())
                .toList();
    }

    private String addLinkWithRecovery(
            long chatId, long userId, URI trackLink, List<String> tags, CommonAddLinkRequest request) {
        try {
            scrapperGateway.addLink(chatId, request);
            return botTextService.get("bot.track.success");
        } catch (LinkAlreadyTrackedException e) {
            return botTextService.get("bot.track.link-already-add");
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

            return retryAfterChatRegistration(chatId, userId, trackLink, tags, request);
        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("client", "scrapper")
                    .addKeyValue("operation", "addLink")
                    .addKeyValue("chat_id", chatId)
                    .addKeyValue("user_id", userId)
                    .addKeyValue("link", trackLink)
                    .addKeyValue("tags", tags)
                    .log("Ошибка при добавлении ссылки");
            return botTextService.get("bot.common.unknown-error");
        }
    }

    private String retryAfterChatRegistration(
            long chatId, long userId, URI trackLink, List<String> tags, CommonAddLinkRequest request) {
        try {
            scrapperGateway.registerChat(chatId);

            log.atInfo()
                    .addKeyValue("client", "scrapper")
                    .addKeyValue("operation", "registerChat")
                    .addKeyValue("chat_id", chatId)
                    .log("Чат зарегистрирован, повторяем addLink один раз");

            scrapperGateway.addLink(chatId, request);
            return botTextService.get("bot.track.success");
        } catch (LinkAlreadyTrackedException e) {
            return botTextService.get("bot.track.link-already-add");
        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("client", "scrapper")
                    .addKeyValue("operation", "addLink.retry")
                    .addKeyValue("chat_id", chatId)
                    .addKeyValue("user_id", userId)
                    .addKeyValue("link", trackLink)
                    .addKeyValue("tags", tags)
                    .log("Ошибка при повторной попытке добавить ссылку после регистрации чата");
            return botTextService.get("bot.common.unknown-error");
        }
    }
}
