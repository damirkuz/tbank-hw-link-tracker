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
@DisplayName("TrackCommandHandler")
class TrackCommandHandlerTest {

    @Mock
    private DialogFlowService dialogFlowService;

    @InjectMocks
    private TrackCommandHandler handler;

    @Test
    @DisplayName("Делегирует старт track-flow")
    void shouldSetStateAndAskForLink() {
        long chatId = 1L;
        long userId = 2L;
        Update update = mock(Update.class);
        UpdateContext ctx = new UpdateContext(1, chatId, userId, "/track", null);

        handler.handle(update, ctx);

        verify(dialogFlowService).startTrackFlow(ctx);
    }
}
