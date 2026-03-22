package backend.academy.linktracker.bot.update.handler.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrackWaitTagsHandler")
class TrackWaitTagsHandlerTest {

    @Mock
    private ScrapperGateway scrapperClient;

    @Mock
    private StateStorage stateStorage;

    @Mock
    private BotTextService botTextService;

    @Mock
    private BotOperations botOperations;

    @InjectMocks
    private TrackWaitTagsHandler handler;

    private static final URI LINK = URI.create("https://github.com/user/repo");

    @Test
    @DisplayName("getHandledState — TRACK_WAIT_TAGS")
    void handledState() {
        assertThat(handler.getHandledState()).isEqualTo(UserState.TRACK_WAIT_TAGS);
    }

    @Test
    @DisplayName("Успешно добавляет ссылку с тегами")
    void shouldAddLinkWithTags() {
        long chatId = 1L;
        long userId = 2L;
        Update update = mockUpdate("java, spring");
        UpdateContext ctx = mockCtx(chatId, userId);
        UserSession session = mockSession(LINK);

        when(botTextService.get("bot.reject")).thenReturn("нет");
        when(botTextService.get("bot.track.success")).thenReturn("Добавлено!");

        handler.handle(update, ctx);

        verify(scrapperClient).addLink(eq(chatId), any(CommonAddLinkRequest.class));
        verify(session).setState(UserState.IDLE);
        verify(session).setTrackLink(null);
        verify(stateStorage).save(userId, session);
        verify(botOperations).sendMessage(chatId, "Добавлено!");
    }

    @Test
    @DisplayName("Добавляет ссылку без тегов если введено reject-слово")
    void shouldAddLinkWithoutTagsWhenRejected() {
        long chatId = 1L;
        long userId = 2L;
        Update update = mockUpdate("нет");
        UpdateContext ctx = mockCtx(chatId, userId);
        mockSession(LINK);

        when(botTextService.get("bot.reject")).thenReturn("нет");
        when(botTextService.get("bot.track.success")).thenReturn("Добавлено!");

        handler.handle(update, ctx);

        verify(scrapperClient).addLink(eq(chatId), any(CommonAddLinkRequest.class));
        verify(botOperations).sendMessage(chatId, "Добавлено!");
    }

    @Test
    @DisplayName("Отправляет ошибку если ссылка уже отслеживается")
    void shouldSendAlreadyTrackedError() {
        long chatId = 1L;
        long userId = 2L;
        Update update = mockUpdate("java");
        UpdateContext ctx = mockCtx(chatId, userId);
        mockSession(LINK);

        when(botTextService.get("bot.reject")).thenReturn("нет");
        doThrow(mock(LinkAlreadyTrackedException.class)).when(scrapperClient).addLink(eq(chatId), any());
        when(botTextService.get("bot.track.link-already-add")).thenReturn("Уже отслеживается.");

        handler.handle(update, ctx);

        verify(botOperations).sendMessage(chatId, "Уже отслеживается.");
    }

    @Test
    @DisplayName("Регистрирует чат и повторяет addLink при ChatNotFoundException")
    void shouldRegisterChatAndRetry() {
        long chatId = 1L;
        long userId = 2L;
        Update update = mockUpdate("java");
        UpdateContext ctx = mockCtx(chatId, userId);
        mockSession(LINK);

        when(botTextService.get("bot.reject")).thenReturn("нет");
        doThrow(new ChatNotFoundException("not found"))
                .doNothing()
                .when(scrapperClient)
                .addLink(eq(chatId), any());
        when(botTextService.get("bot.track.success")).thenReturn("Добавлено!");

        handler.handle(update, ctx);

        verify(scrapperClient).registerChat(chatId);
        verify(botOperations).sendMessage(chatId, "Добавлено!");
    }

    @Test
    @DisplayName("Отправляет unknown-error при неизвестном исключении")
    void shouldSendUnknownErrorOnException() {
        long chatId = 1L;
        long userId = 2L;
        Update update = mockUpdate("java");
        UpdateContext ctx = mockCtx(chatId, userId);
        mockSession(LINK);

        when(botTextService.get("bot.reject")).thenReturn("нет");
        doThrow(new RuntimeException("unexpected")).when(scrapperClient).addLink(eq(chatId), any());
        when(botTextService.get("bot.common.unknown-error")).thenReturn("Что-то пошло не так.");

        handler.handle(update, ctx);

        verify(botOperations).sendMessage(chatId, "Что-то пошло не так.");
    }

    private Update mockUpdate(String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        return update;
    }

    private UpdateContext mockCtx(long chatId, long userId) {
        UpdateContext ctx = mock(UpdateContext.class);
        when(ctx.chatId()).thenReturn(chatId);
        when(ctx.userId()).thenReturn(userId);
        return ctx;
    }

    private UserSession mockSession(URI link) {
        UserSession session = mock(UserSession.class);
        when(session.getTrackLink()).thenReturn(link);
        when(stateStorage.getUserSession(org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(session);
        return session;
    }
}
