package backend.academy.linktracker.bot.update.handler.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.validator.link.LinkValidationResult;
import backend.academy.linktracker.bot.validator.link.LinkValidationService;
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
@DisplayName("TrackWaitLinkHandler")
class TrackWaitLinkHandlerTest {

    @Mock
    private LinkValidationService linkValidationService;

    @Mock
    private BotTextService botTextService;

    @Mock
    private BotOperations botOperations;

    @Mock
    private StateStorage stateStorage;

    @InjectMocks
    private TrackWaitLinkHandler handler;

    @Test
    @DisplayName("getHandledState — TRACK_WAIT_LINK")
    void handledState() {
        assertThat(handler.getHandledState()).isEqualTo(UserState.TRACK_WAIT_LINK);
    }

    @Test
    @DisplayName("Валидная ссылка — переводит в TRACK_WAIT_TAGS и спрашивает теги")
    void shouldTransitionToWaitTagsOnValidLink() {
        long chatId = 1L;
        long userId = 2L;
        String link = "https://github.com/user/repo";
        Update update = mockUpdate(link);
        UpdateContext ctx = mockCtx(chatId, userId);
        UserSession session = mock(UserSession.class);

        when(linkValidationService.validate(link)).thenReturn(new LinkValidationResult(true, null, null));
        when(stateStorage.getUserSession(userId)).thenReturn(session);
        when(botTextService.get("bot.track.ask-tags")).thenReturn("Введите теги:");

        handler.handle(update, ctx);

        verify(session).setState(UserState.TRACK_WAIT_TAGS);
        verify(session).setTrackLink(URI.create(link));
        verify(stateStorage).save(userId, session);
        verify(botOperations).sendMessage(chatId, "Введите теги:");
    }

    @Test
    @DisplayName("Невалидная ссылка — не меняет состояние и просит ввести снова")
    void shouldStayInStateOnInvalidLink() {
        long chatId = 1L;
        long userId = 2L;
        String link = "not-a-link";
        Update update = mockUpdate(link);
        UpdateContext ctx = mockCtx(chatId, userId);

        when(linkValidationService.validate(link)).thenReturn(new LinkValidationResult(false, "invalid", null));
        when(botTextService.get("bot.track.invalid-link")).thenReturn("Некорректная ссылка.");

        handler.handle(update, ctx);

        verify(stateStorage, never()).save(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
        verify(botOperations).sendMessage(chatId, "Некорректная ссылка.");
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
}
