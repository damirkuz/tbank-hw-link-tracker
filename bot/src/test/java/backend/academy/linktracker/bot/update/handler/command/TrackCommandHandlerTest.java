package backend.academy.linktracker.bot.update.handler.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrackCommandHandler")
class TrackCommandHandlerTest {

    @Mock
    private BotTextService botTextService;

    @Mock
    private StateStorage stateStorage;

    @Mock
    private BotOperations botOperations;

    @Mock
    private ReplyKeyboardFactory replyKeyboardFactory;

    @InjectMocks
    private TrackCommandHandler handler;

    @Test
    @DisplayName("Устанавливает состояние TRACK_WAIT_LINK и отправляет запрос ссылки")
    void shouldSetStateAndAskForLink() {
        long chatId = 1L;
        long userId = 2L;
        Update update = mock(Update.class);
        UpdateContext ctx = mock(UpdateContext.class);
        when(ctx.chatId()).thenReturn(chatId);
        when(ctx.userId()).thenReturn(userId);
        when(botTextService.get("bot.track.ask-link")).thenReturn("Введите ссылку:");
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(replyKeyboardFactory.cancelOnly()).thenReturn(keyboard);

        handler.handle(update, ctx);

        verify(stateStorage).updateState(userId, UserState.TRACK_WAIT_LINK);
        verify(botOperations).sendMessage(chatId, "Введите ссылку:", keyboard);
    }
}
