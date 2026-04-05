package backend.academy.linktracker.bot.update.handler.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.usecase.UntrackFlowService;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UntrackWaitLinkHandler")
class UntrackWaitLinkHandlerTest {

    @Mock
    private UntrackFlowService untrackFlowService;

    @InjectMocks
    private UntrackWaitLinkHandler handler;

    @Test
    @DisplayName("getHandledState — UNTRACK_WAIT_LINK")
    void handledState() {
        assertThat(handler.getHandledState()).isEqualTo(UserState.UNTRACK_WAIT_LINK);
    }

    @Test
    @DisplayName("Делегирует обработку UntrackFlowService")
    void shouldDelegateToUntrackFlowService() {
        long chatId = 1L;
        long userId = 2L;
        String link = "https://github.com/user/repo";
        Update update = mockUpdate(link);
        UpdateContext ctx = new UpdateContext(1, chatId, userId, null, null);

        handler.handle(update, ctx);

        verify(untrackFlowService).handleLinkInput(ctx, link);
    }

    private Update mockUpdate(String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        return update;
    }
}
