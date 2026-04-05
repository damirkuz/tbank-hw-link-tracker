package backend.academy.linktracker.bot.update.router;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.model.UserChatKey;
import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.update.handler.UnknownUpdateHandler;
import backend.academy.linktracker.bot.update.handler.state.StateHandler;
import backend.academy.linktracker.bot.update.handler.state.StateHandlerRegistry;
import com.pengrad.telegrambot.model.Update;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StateRouter")
class StateRouterTest {

    @Mock
    private StateStorage stateStorage;

    @Mock
    private StateHandlerRegistry handlerRegistry;

    @Mock
    private UnknownUpdateHandler unknownUpdateHandler;

    @Mock
    private StateHandler stateHandler;

    @InjectMocks
    private StateRouter stateRouter;

    @Test
    @DisplayName("Маршрутизирует по состоянию конкретного userId/chatId")
    void shouldRouteUsingDialogScopedState() {
        Update update = mock(Update.class);
        UpdateContext context = new UpdateContext(1, 100L, 7L, "text", null);
        when(stateStorage.getUserSession(new UserChatKey(7L, 100L)))
                .thenReturn(new UserSession(UserState.TRACK_WAIT_LINK));
        when(handlerRegistry.getHandler(UserState.TRACK_WAIT_LINK)).thenReturn(Optional.of(stateHandler));

        stateRouter.route(update, context);

        verify(stateHandler).handle(update, context);
        verifyNoInteractions(unknownUpdateHandler);
    }

    @Test
    @DisplayName("Если chatId отсутствует, передаёт update в unknown handler")
    void shouldFallbackToUnknownWhenChatIdMissing() {
        Update update = mock(Update.class);
        UpdateContext context = new UpdateContext(1, null, 7L, "text", null);

        stateRouter.route(update, context);

        verify(unknownUpdateHandler).handle(update, context);
        verifyNoInteractions(handlerRegistry);
    }
}
