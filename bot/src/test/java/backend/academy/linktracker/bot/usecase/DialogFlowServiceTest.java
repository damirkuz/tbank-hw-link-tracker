package backend.academy.linktracker.bot.usecase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.model.InterruptionPolicy;
import backend.academy.linktracker.bot.model.UserChatKey;
import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DialogFlowService")
class DialogFlowServiceTest {

    private final StateStorage stateStorage = mock(StateStorage.class);
    private final BotTextService botTextService = mock(BotTextService.class);
    private final BotOperations botOperations = mock(BotOperations.class);
    private final ReplyKeyboardFactory replyKeyboardFactory = mock(ReplyKeyboardFactory.class);

    private final DialogFlowService service =
            new DialogFlowService(stateStorage, botTextService, botOperations, replyKeyboardFactory);

    @Test
    @DisplayName("startTrackFlow переводит в TRACK_WAIT_LINK и показывает cancel keyboard")
    void startTrackFlow() {
        UpdateContext context = new UpdateContext(1, 1L, 2L, "/track", null);
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(botTextService.get("bot.track.ask-link")).thenReturn("Введи ссылку");
        when(replyKeyboardFactory.cancelOnly()).thenReturn(keyboard);

        service.startTrackFlow(context);

        verify(stateStorage).updateState(new UserChatKey(2L, 1L), UserState.TRACK_WAIT_LINK);
        verify(botOperations).sendMessage(1L, "Введи ссылку", keyboard);
    }

    @Test
    @DisplayName("startUntrackFlow переводит в UNTRACK_WAIT_LINK и показывает cancel keyboard")
    void startUntrackFlow() {
        UpdateContext context = new UpdateContext(1, 1L, 2L, "/untrack", null);
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(botTextService.get("bot.untrack.ask-link")).thenReturn("Введи ссылку");
        when(replyKeyboardFactory.cancelOnly()).thenReturn(keyboard);

        service.startUntrackFlow(context);

        verify(stateStorage).updateState(new UserChatKey(2L, 1L), UserState.UNTRACK_WAIT_LINK);
        verify(botOperations).sendMessage(1L, "Введи ссылку", keyboard);
    }

    @Test
    @DisplayName("startConversation сбрасывает state и показывает главное меню")
    void startConversation() {
        UpdateContext context = new UpdateContext(1, 1L, 2L, "/start", null);
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(botTextService.get("bot.common.start")).thenReturn("Старт");
        when(replyKeyboardFactory.mainMenu()).thenReturn(keyboard);

        service.startConversation(context);

        verify(stateStorage).clearState(new UserChatKey(2L, 1L));
        verify(botOperations).sendMessage(1L, "Старт", keyboard);
    }

    @Test
    @DisplayName("cancelConversation сообщает что отменять нечего")
    void cancelConversationWhenIdle() {
        UpdateContext context = new UpdateContext(1, 1L, 2L, "/cancel", null);
        UserSession session = mock(UserSession.class);
        when(stateStorage.getUserSession(new UserChatKey(2L, 1L))).thenReturn(session);
        when(session.getState()).thenReturn(UserState.IDLE);
        when(botTextService.get("bot.common.nothing-to-cancel")).thenReturn("Нечего отменять");

        service.cancelConversation(context);

        verify(botOperations).sendMessage(1L, "Нечего отменять");
        verify(stateStorage, never()).clearState(new UserChatKey(2L, 1L));
    }

    @Test
    @DisplayName("cancelConversation блокирует отмену для BLOCK_ALL")
    void cancelConversationWhenBlocked() {
        UpdateContext context = new UpdateContext(1, 1L, 2L, "/cancel", null);
        UserSession session = mock(UserSession.class);
        when(stateStorage.getUserSession(new UserChatKey(2L, 1L))).thenReturn(session);
        when(session.getState()).thenReturn(UserState.TRACK_WAIT_TAGS);
        when(session.getInterruptionPolicy()).thenReturn(InterruptionPolicy.BLOCK_ALL);
        when(botTextService.get("bot.common.cancel-blocked")).thenReturn("Отмена запрещена");

        service.cancelConversation(context);

        verify(botOperations).sendMessage(1L, "Отмена запрещена");
        verify(stateStorage, never()).clearState(new UserChatKey(2L, 1L));
    }

    @Test
    @DisplayName("cancelConversation очищает state и показывает главное меню")
    void cancelConversationSuccess() {
        UpdateContext context = new UpdateContext(1, 1L, 2L, "/cancel", null);
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        UserSession session = mock(UserSession.class);
        when(stateStorage.getUserSession(new UserChatKey(2L, 1L))).thenReturn(session);
        when(session.getState()).thenReturn(UserState.TRACK_WAIT_LINK);
        when(session.getInterruptionPolicy()).thenReturn(InterruptionPolicy.ALLOW_ALL);
        when(botTextService.get("bot.common.cancelled")).thenReturn("Отменено");
        when(replyKeyboardFactory.mainMenu()).thenReturn(keyboard);

        service.cancelConversation(context);

        verify(stateStorage).clearState(new UserChatKey(2L, 1L));
        verify(botOperations).sendMessage(1L, "Отменено", keyboard);
    }
}
