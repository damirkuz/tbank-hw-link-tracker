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
@DisplayName("UntrackCommandHandler")
class UntrackCommandHandlerTest {

    @Mock private BotTextService botTextService;
    @Mock private BotOperations botOperations;
    @Mock private StateStorage stateStorage;
    @Mock private ReplyKeyboardFactory replyKeyboardFactory;

    @InjectMocks
    private UntrackCommandHandler handler;

    @Test
    @DisplayName("Переводит в состояние UNTRACK_WAIT_LINK и спрашивает ссылку")
    void shouldSetStateAndAskForLink() {
        long chatId = 1L;
        long userId = 2L;
        UpdateContext ctx = mock(UpdateContext.class);
        when(ctx.chatId()).thenReturn(chatId);
        when(ctx.userId()).thenReturn(userId);
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(replyKeyboardFactory.cancelOnly()).thenReturn(keyboard);
        when(botTextService.get("bot.untrack.ask-link")).thenReturn("Введи ссылку для удаления:");

        handler.handle(mock(Update.class), ctx);

        verify(stateStorage).updateState(userId, UserState.UNTRACK_WAIT_LINK);
        verify(botOperations).sendMessage(chatId, "Введи ссылку для удаления:", keyboard);
    }
}

