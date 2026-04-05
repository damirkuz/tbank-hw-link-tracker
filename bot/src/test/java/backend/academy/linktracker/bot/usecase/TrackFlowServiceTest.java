package backend.academy.linktracker.bot.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrackFlowService")
class TrackFlowServiceTest {

    private static final URI LINK = URI.create("https://github.com/user/repo");

    @Mock
    private LinkValidationService linkValidationService;

    @Mock
    private ScrapperGateway scrapperGateway;

    @Mock
    private StateStorage stateStorage;

    @Mock
    private BotTextService botTextService;

    @Mock
    private BotOperations botOperations;

    @Mock
    private ReplyKeyboardFactory replyKeyboardFactory;

    @Test
    @DisplayName("Валидная ссылка переводит диалог в ожидание тегов")
    void shouldTransitionToWaitTagsOnValidLink() {
        TrackFlowService service = service();
        long chatId = 1L;
        long userId = 2L;
        UpdateContext ctx = new UpdateContext(1, chatId, userId, null, null);
        UserSession session = mock(UserSession.class);

        when(linkValidationService.validate(LINK.toString())).thenReturn(new LinkValidationResult(true, null, null));
        when(stateStorage.getUserSession(new UserChatKey(userId, chatId))).thenReturn(session);
        when(botTextService.get("bot.track.ask-tags")).thenReturn("Введите теги:");

        service.handleLinkInput(ctx, LINK.toString());

        verify(session).setState(UserState.TRACK_WAIT_TAGS);
        verify(session).setTrackLink(LINK);
        verify(stateStorage).save(new UserChatKey(userId, chatId), session);
        verify(botOperations).sendMessage(chatId, "Введите теги:");
    }

    @Test
    @DisplayName("Невалидная ссылка не меняет состояние")
    void shouldStayInStateOnInvalidLink() {
        TrackFlowService service = service();
        long chatId = 1L;
        UpdateContext ctx = new UpdateContext(1, chatId, 2L, null, null);

        when(linkValidationService.validate("not-a-link")).thenReturn(new LinkValidationResult(false, "invalid", null));
        when(botTextService.get("bot.track.invalid-link")).thenReturn("Некорректная ссылка.");

        service.handleLinkInput(ctx, "not-a-link");

        verify(stateStorage, never()).save(any(UserChatKey.class), any(UserSession.class));
        verify(botOperations).sendMessage(chatId, "Некорректная ссылка.");
    }

    @Test
    @DisplayName("Успешно добавляет ссылку с тегами")
    void shouldAddLinkWithTags() {
        TrackFlowService service = service();
        long chatId = 1L;
        long userId = 2L;
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        UserSession session = mockTrackedSession(chatId, userId);

        when(replyKeyboardFactory.mainMenu()).thenReturn(keyboard);
        when(botTextService.get("bot.reject")).thenReturn("нет");
        when(botTextService.get("bot.track.success")).thenReturn("Добавлено!");

        service.handleTagsInput(new UpdateContext(1, chatId, userId, null, null), "java, spring");

        verify(scrapperGateway).addLink(eq(chatId), any(CommonAddLinkRequest.class));
        verify(session).setTrackLink(null);
        verify(session).setState(UserState.IDLE);
        verify(stateStorage).save(new UserChatKey(userId, chatId), session);
        verify(botOperations).sendMessage(chatId, "Добавлено!", keyboard);
    }

    @Test
    @DisplayName("Использует пустой список тегов для reject-слова")
    void shouldAddLinkWithoutTagsWhenRejected() {
        TrackFlowService service = service();
        long chatId = 1L;
        long userId = 2L;
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        mockTrackedSession(chatId, userId);

        when(replyKeyboardFactory.mainMenu()).thenReturn(keyboard);
        when(botTextService.get("bot.reject")).thenReturn("нет");
        when(botTextService.get("bot.track.success")).thenReturn("Добавлено!");

        service.handleTagsInput(new UpdateContext(1, chatId, userId, null, null), "нет");

        verify(scrapperGateway)
                .addLink(eq(chatId), eq(new CommonAddLinkRequest(LINK, java.util.List.of(), java.util.List.of())));
        verify(botOperations).sendMessage(chatId, "Добавлено!", keyboard);
    }

    @Test
    @DisplayName("Регистрирует чат и повторяет addLink при ChatNotFoundException")
    void shouldRegisterChatAndRetry() {
        TrackFlowService service = service();
        long chatId = 1L;
        long userId = 2L;
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        mockTrackedSession(chatId, userId);

        when(replyKeyboardFactory.mainMenu()).thenReturn(keyboard);
        when(botTextService.get("bot.reject")).thenReturn("нет");
        doThrow(new ChatNotFoundException("not found"))
                .doNothing()
                .when(scrapperGateway)
                .addLink(eq(chatId), any());
        when(botTextService.get("bot.track.success")).thenReturn("Добавлено!");

        service.handleTagsInput(new UpdateContext(1, chatId, userId, null, null), "java");

        verify(scrapperGateway).registerChat(chatId);
        verify(botOperations).sendMessage(chatId, "Добавлено!", keyboard);
    }

    @Test
    @DisplayName("Возвращает known answer если ссылка уже отслеживается")
    void shouldSendAlreadyTrackedError() {
        TrackFlowService service = service();
        long chatId = 1L;
        long userId = 2L;
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        mockTrackedSession(chatId, userId);

        when(replyKeyboardFactory.mainMenu()).thenReturn(keyboard);
        when(botTextService.get("bot.reject")).thenReturn("нет");
        doThrow(mock(LinkAlreadyTrackedException.class)).when(scrapperGateway).addLink(eq(chatId), any());
        when(botTextService.get("bot.track.link-already-add")).thenReturn("Уже отслеживается.");

        service.handleTagsInput(new UpdateContext(1, chatId, userId, null, null), "java");

        verify(botOperations).sendMessage(chatId, "Уже отслеживается.", keyboard);
    }

    @Test
    @DisplayName("При неизвестной ошибке не теряет управление и отвечает unknown-error")
    void shouldSendUnknownErrorOnException() {
        TrackFlowService service = service();
        long chatId = 1L;
        long userId = 2L;
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        mockTrackedSession(chatId, userId);

        when(replyKeyboardFactory.mainMenu()).thenReturn(keyboard);
        when(botTextService.get("bot.reject")).thenReturn("нет");
        doThrow(new RuntimeException("unexpected")).when(scrapperGateway).addLink(eq(chatId), any());
        when(botTextService.get("bot.common.unknown-error")).thenReturn("Что-то пошло не так.");

        service.handleTagsInput(new UpdateContext(1, chatId, userId, null, null), "java");

        verify(botOperations).sendMessage(chatId, "Что-то пошло не так.", keyboard);
    }

    private TrackFlowService service() {
        return new TrackFlowService(
                linkValidationService,
                scrapperGateway,
                stateStorage,
                botTextService,
                botOperations,
                replyKeyboardFactory);
    }

    private UserSession mockTrackedSession(long chatId, long userId) {
        UserSession session = mock(UserSession.class);
        when(session.getTrackLink()).thenReturn(LINK);
        when(stateStorage.getUserSession(new UserChatKey(userId, chatId))).thenReturn(session);
        return session;
    }
}
