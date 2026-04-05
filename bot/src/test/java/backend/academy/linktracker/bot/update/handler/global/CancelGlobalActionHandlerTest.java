package backend.academy.linktracker.bot.update.handler.global;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.config.properties.CommandMessage;
import backend.academy.linktracker.bot.model.InterruptionPolicy;
import backend.academy.linktracker.bot.model.UserChatKey;
import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelGlobalActionHandler")
class CancelGlobalActionHandlerTest {

    @Mock
    private BotOperations botOperations;

    @Mock
    private BotTextService botTextService;

    @Mock
    private StateStorage stateStorage;

    @Mock
    private ReplyKeyboardFactory replyKeyboardFactory;

    private CancelGlobalActionHandler handler;

    @BeforeEach
    void setUp() {
        BotProperties botProperties = new BotProperties(
                java.util.Map.of("cancel", new CommandMessage("/cancel", "Cancel", java.util.List.of())),
                new BotProperties.TagKeyboard(20, 8, 3, "list_tag:", "list_tags_all", "list_tag_input"));
        handler = new CancelGlobalActionHandler(
                botOperations, botTextService, stateStorage, replyKeyboardFactory, botProperties);
    }

    @Test
    @DisplayName("supports — true для команды /cancel")
    void supportsCancel() {
        UpdateContext ctx = new UpdateContext(1, 1L, 1L, "/cancel", null);
        assertThat(handler.supports(ctx)).isTrue();
    }

    @Test
    @DisplayName("Отправляет nothing-to-cancel если состояние IDLE")
    void shouldSendNothingToCancelWhenIdle() {
        long chatId = 1L;
        long userId = 2L;
        UpdateContext ctx = mockCtx(chatId, userId);
        UserSession session = mock(UserSession.class);
        when(stateStorage.getUserSession(new UserChatKey(userId, chatId))).thenReturn(session);
        when(session.getState()).thenReturn(UserState.IDLE);
        when(botTextService.get("bot.common.nothing-to-cancel")).thenReturn("Нечего отменять.");

        handler.handle(mock(Update.class), ctx);

        verify(botOperations).sendMessage(chatId, "Нечего отменять.");
        verify(stateStorage, never()).clearState(new UserChatKey(userId, chatId));
    }

    @Test
    @DisplayName("Отправляет cancel-blocked если политика BLOCK_ALL")
    void shouldSendBlockedWhenPolicyBlockAll() {
        long chatId = 1L;
        long userId = 2L;
        UpdateContext ctx = mockCtx(chatId, userId);
        UserSession session = mock(UserSession.class);
        when(stateStorage.getUserSession(new UserChatKey(userId, chatId))).thenReturn(session);
        when(session.getState()).thenReturn(UserState.TRACK_WAIT_TAGS);
        when(session.getInterruptionPolicy()).thenReturn(InterruptionPolicy.BLOCK_ALL);
        when(botTextService.get("bot.common.cancel-blocked")).thenReturn("Отмена заблокирована.");

        handler.handle(mock(Update.class), ctx);

        verify(botOperations).sendMessage(chatId, "Отмена заблокирована.");
        verify(stateStorage, never()).clearState(new UserChatKey(userId, chatId));
    }

    @Test
    @DisplayName("Сбрасывает состояние и отправляет cancelled с главным меню")
    void shouldClearStateAndSendCancelled() {
        long chatId = 1L;
        long userId = 2L;
        UpdateContext ctx = mockCtx(chatId, userId);
        UserSession session = mock(UserSession.class);
        when(stateStorage.getUserSession(new UserChatKey(userId, chatId))).thenReturn(session);
        when(session.getState()).thenReturn(UserState.TRACK_WAIT_LINK);
        when(session.getInterruptionPolicy()).thenReturn(InterruptionPolicy.ALLOW_ALL);
        when(botTextService.get("bot.common.cancelled")).thenReturn("Отменено.");
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(replyKeyboardFactory.mainMenu()).thenReturn(keyboard);

        handler.handle(mock(Update.class), ctx);

        verify(stateStorage).clearState(new UserChatKey(userId, chatId));
        verify(botOperations).sendMessage(chatId, "Отменено.", keyboard);
    }

    @Test
    @DisplayName("Ничего не делает если chatId == null")
    void shouldDoNothingWhenChatIdNull() {
        UpdateContext ctx = new UpdateContext(1, null, 2L, "/cancel", null);

        handler.handle(mock(Update.class), ctx);

        verify(botOperations, never()).sendMessage(anyLong(), anyString());
        verify(botOperations, never()).sendMessage(anyLong(), anyString(), any());
    }

    private UpdateContext mockCtx(long chatId, long userId) {
        return new UpdateContext(1, chatId, userId, "/cancel", null);
    }
}
