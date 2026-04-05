package backend.academy.linktracker.bot.update;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.update.context.UpdateEnvelope;
import backend.academy.linktracker.bot.update.context.UpdateEnvelopeFactory;
import backend.academy.linktracker.bot.update.router.CallbackRouter;
import backend.academy.linktracker.bot.update.router.CommandRouter;
import backend.academy.linktracker.bot.update.router.GlobalActionRouter;
import backend.academy.linktracker.bot.update.router.StateRouter;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateDispatcher")
class UpdateDispatcherTest {

    @Mock
    private TelegramBot bot;

    @Mock
    private CommandRouter commandRouter;

    @Mock
    private StateRouter stateRouter;

    @Mock
    private CallbackRouter callbackRouter;

    @Mock
    private GlobalActionRouter globalActionRouter;

    @Mock
    private UpdateEnvelopeFactory updateEnvelopeFactory;

    @InjectMocks
    private UpdateDispatcher updateDispatcher;

    @Test
    @DisplayName("Глобальный роутер обрабатывает — остальные не вызываются")
    void shouldStopWhenGlobalRouterHandles() {
        Update update = mock(Update.class);
        UpdateContext ctx = mockContext(false, false, null);
        UpdateEnvelope envelope = new UpdateEnvelope(update, ctx);
        when(updateEnvelopeFactory.create(update)).thenReturn(envelope);
        when(globalActionRouter.tryHandle(update, ctx)).thenReturn(true);

        updateDispatcher.dispatch(update);

        verify(globalActionRouter).tryHandle(update, ctx);
        verifyNoInteractions(callbackRouter, commandRouter, stateRouter);
    }

    @Test
    @DisplayName("Callback направляется в CallbackRouter")
    void shouldRouteToCallbackRouter() {
        Update update = mock(Update.class);
        UpdateContext ctx = mockContext(false, true, null);
        UpdateEnvelope envelope = new UpdateEnvelope(update, ctx);
        when(updateEnvelopeFactory.create(update)).thenReturn(envelope);
        when(globalActionRouter.tryHandle(update, ctx)).thenReturn(false);

        updateDispatcher.dispatch(update);

        verify(callbackRouter).route(update, ctx);
        verifyNoInteractions(commandRouter, stateRouter);
    }

    @Test
    @DisplayName("Команда направляется в CommandRouter")
    void shouldRouteCommandToCommandRouter() {
        Update update = mock(Update.class);
        UpdateContext ctx = mockContext(true, false, "/start");
        UpdateEnvelope envelope = new UpdateEnvelope(update, ctx);
        when(updateEnvelopeFactory.create(update)).thenReturn(envelope);
        when(globalActionRouter.tryHandle(update, ctx)).thenReturn(false);

        updateDispatcher.dispatch(update);

        verify(commandRouter).route(update, ctx);
        verifyNoInteractions(callbackRouter, stateRouter);
    }

    @Test
    @DisplayName("Обычный текст направляется в StateRouter")
    void shouldRouteTextToStateRouter() {
        Update update = mock(Update.class);
        UpdateContext ctx = mockContext(true, false, "просто текст");
        UpdateEnvelope envelope = new UpdateEnvelope(update, ctx);
        when(updateEnvelopeFactory.create(update)).thenReturn(envelope);
        when(globalActionRouter.tryHandle(update, ctx)).thenReturn(false);

        updateDispatcher.dispatch(update);

        verify(stateRouter).route(update, ctx);
        verifyNoInteractions(callbackRouter, commandRouter);
    }

    @Test
    @DisplayName("Пустой контекст без текста и callback направляется в StateRouter")
    void shouldRouteEmptyContextToStateRouter() {
        Update update = mock(Update.class);
        UpdateContext ctx = mockContext(false, false, null);
        UpdateEnvelope envelope = new UpdateEnvelope(update, ctx);
        when(updateEnvelopeFactory.create(update)).thenReturn(envelope);
        when(globalActionRouter.tryHandle(update, ctx)).thenReturn(false);

        updateDispatcher.dispatch(update);

        verify(stateRouter).route(update, ctx);
        verifyNoInteractions(callbackRouter, commandRouter);
    }

    private UpdateContext mockContext(boolean hasText, boolean hasCallback, String messageText) {
        String text = hasText ? messageText : null;
        String callback = hasCallback ? "callback" : null;
        return new UpdateContext(1, 1L, 1L, text, callback);
    }
}
