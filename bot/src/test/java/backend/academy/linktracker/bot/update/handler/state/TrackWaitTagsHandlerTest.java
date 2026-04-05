package backend.academy.linktracker.bot.update.handler.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.usecase.TrackFlowService;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
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
    private TrackFlowService trackFlowService;

    @InjectMocks
    private TrackWaitTagsHandler handler;

    @Test
    @DisplayName("getHandledState — TRACK_WAIT_TAGS")
    void handledState() {
        assertThat(handler.getHandledState()).isEqualTo(UserState.TRACK_WAIT_TAGS);
    }

    @Test
    @DisplayName("Делегирует обработку TrackFlowService")
    void shouldDelegateToTrackFlowService() {
        long chatId = 1L;
        long userId = 2L;

        handler.handle(mockUpdate("java, spring"), mockCtx(chatId, userId));

        verify(trackFlowService).handleTagsInput(mockCtx(chatId, userId), "java, spring");
    }

    private Update mockUpdate(String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        return update;
    }

    private UpdateContext mockCtx(long chatId, long userId) {
        return new UpdateContext(1, chatId, userId, null, null);
    }
}
