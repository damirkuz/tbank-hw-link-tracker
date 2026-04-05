package backend.academy.linktracker.bot.update.handler.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.usecase.DialogFlowService;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UntrackCommandHandler")
class UntrackCommandHandlerTest {

    @Mock
    private DialogFlowService dialogFlowService;

    @InjectMocks
    private UntrackCommandHandler handler;

    @Test
    @DisplayName("Делегирует старт untrack-flow")
    void shouldSetStateAndAskForLink() {
        long chatId = 1L;
        long userId = 2L;
        UpdateContext ctx = new UpdateContext(1, chatId, userId, "/untrack", null);

        handler.handle(mock(Update.class), ctx);

        verify(dialogFlowService).startUntrackFlow(ctx);
    }
}
