// package backend.academy.linktracker.bot.update;
//
// import static org.mockito.ArgumentMatchers.isNull;
// import static org.mockito.ArgumentMatchers.same;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.verifyNoInteractions;
// import static org.mockito.Mockito.when;
//
// import backend.academy.linktracker.bot.model.UserSession;
// import backend.academy.linktracker.bot.model.UserState;
// import backend.academy.linktracker.bot.service.StateStorage;
// import backend.academy.linktracker.bot.update.handler.command.CommandHandler;
// import backend.academy.linktracker.bot.update.handler.command.CommandHandlerRegistry;
// import backend.academy.linktracker.bot.update.router.CommandRouter;
// import backend.academy.linktracker.bot.update.router.IdleRouter;
// import backend.academy.linktracker.bot.update.router.StateRouter;
// import com.pengrad.telegrambot.model.Chat;
// import com.pengrad.telegrambot.model.Message;
// import com.pengrad.telegrambot.model.Update;
// import com.pengrad.telegrambot.model.User;
// import java.util.Optional;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
//
// @ExtendWith(MockitoExtension.class)
// @DisplayName("UpdateDispatcher")
// class UpdateDispatcherTest {
//
//    @Mock
//    private CommandRouter commandRouter;
//
//    @Mock
//    private IdleRouter idleRouter;
//
//    @Mock
//    private StateRouter stateRouter;
//
//    @Mock
//    private StateStorage stateStorage;
//
//    @Mock
//    private CommandHandlerRegistry commandHandlerRegistry;
//
//    @Mock
//    private CommandHandler commandHandler;
//
//    @Mock
//    private UserSession userSession;
//
//    @InjectMocks
//    private UpdateDispatcher updateDispatcher;
//
//    @Test
//    @DisplayName("Игнорирует update без message")
//    void shouldIgnoreUpdateWithoutMessage() {
//        Update update = mock(Update.class);
//        when(update.message()).thenReturn(null);
//
//        updateDispatcher.dispatch(update);
//
//        verifyNoInteractions(commandRouter, idleRouter, stateRouter, stateStorage, commandHandlerRegistry);
//    }
//
//    @Test
//    @DisplayName("Игнорирует update без chat")
//    void shouldIgnoreUpdateWithoutChat() {
//        Update update = mock(Update.class);
//        Message message = mock(Message.class);
//
//        when(update.message()).thenReturn(message);
//        when(message.chat()).thenReturn(null);
//
//        updateDispatcher.dispatch(update);
//
//        verifyNoInteractions(commandRouter, idleRouter, stateRouter, stateStorage, commandHandlerRegistry);
//    }
//
//    @Test
//    @DisplayName("Сбрасывает состояние и отправляет cancel-команду в CommandRouter")
//    void shouldResetStateAndRouteCancelCommand() {
//        long chatId = 1L;
//        long userId = 11L;
//        Update update = createUpdate(chatId, userId, "/start");
//
//        when(stateStorage.getUserSession(userId)).thenReturn(userSession);
//        when(userSession.getState()).thenReturn(UserState.TRACK_WAIT_LINK);
//        when(commandHandlerRegistry.findByCommandText("/start")).thenReturn(Optional.of(commandHandler));
//        when(commandHandler.isCancelStateCommand()).thenReturn(true);
//
//        updateDispatcher.dispatch(update);
//
//        verify(commandHandlerRegistry).findByCommandText("/start");
//        verify(userSession).getState();
//        verify(stateStorage).updateState(userId, UserState.IDLE);
//        verify(commandRouter).route(same(update), same(commandHandler));
//        verify(idleRouter, never()).route(same(update));
//        verify(stateRouter, never()).route(same(update));
//    }
//
//    @Test
//    @DisplayName("Маршрутизирует обычную команду в CommandRouter при состоянии IDLE")
//    void shouldRouteCommandToCommandRouterWhenStateIsIdle() {
//        long chatId = 2L;
//        long userId = 22L;
//        Update update = createUpdate(chatId, userId, "/help");
//
//        when(stateStorage.getUserSession(userId)).thenReturn(userSession);
//        when(userSession.getState()).thenReturn(UserState.IDLE);
//        when(commandHandlerRegistry.findByCommandText("/help")).thenReturn(Optional.of(commandHandler));
//        when(commandHandler.isCancelStateCommand()).thenReturn(false);
//
//        updateDispatcher.dispatch(update);
//
//        verify(commandHandlerRegistry).findByCommandText("/help");
//        verify(userSession).getState();
//        verify(commandRouter).route(same(update), same(commandHandler));
//        verify(userSession, never()).setState(UserState.IDLE);
//        verify(idleRouter, never()).route(same(update));
//        verify(stateRouter, never()).route(same(update));
//    }
//
//    @Test
//    @DisplayName("Маршрутизирует текст в StateRouter при не-IDLE состоянии")
//    void shouldRouteTextToStateRouterWhenStateIsNotIdle() {
//        long chatId = 3L;
//        long userId = 33L;
//        Update update = createUpdate(chatId, userId, "https://example.com");
//
//        when(stateStorage.getUserSession(userId)).thenReturn(userSession);
//        when(userSession.getState()).thenReturn(UserState.TRACK_WAIT_LINK);
//        when(commandHandlerRegistry.findByCommandText(null)).thenReturn(Optional.empty());
//
//        updateDispatcher.dispatch(update);
//
//        verify(commandHandlerRegistry).findByCommandText(isNull());
//        verify(userSession).getState();
//        verify(stateRouter).route(same(update));
//        verify(commandRouter, never()).route(same(update), same(commandHandler));
//        verify(idleRouter, never()).route(same(update));
//    }
//
//    @Test
//    @DisplayName("Маршрутизирует неизвестный текст в IdleRouter при состоянии IDLE")
//    void shouldRouteUnknownTextToIdleRouterWhenStateIsIdle() {
//        long chatId = 4L;
//        long userId = 44L;
//        Update update = createUpdate(chatId, userId, "Просто текст");
//
//        when(stateStorage.getUserSession(userId)).thenReturn(userSession);
//        when(userSession.getState()).thenReturn(UserState.IDLE);
//        when(commandHandlerRegistry.findByCommandText(null)).thenReturn(Optional.empty());
//
//        updateDispatcher.dispatch(update);
//
//        verify(commandHandlerRegistry).findByCommandText(isNull());
//        verify(userSession).getState();
//        verify(idleRouter).route(same(update));
//        verify(commandRouter, never()).route(same(update), same(commandHandler));
//        verify(stateRouter, never()).route(same(update));
//    }
//
//    private Update createUpdate(long chatId, long userId, String text) {
//        Update update = mock(Update.class);
//        Message message = mock(Message.class);
//        Chat chat = mock(Chat.class);
//        User user = mock(User.class);
//
//        when(update.message()).thenReturn(message);
//        when(message.chat()).thenReturn(chat);
//        when(chat.id()).thenReturn(chatId);
//        when(message.from()).thenReturn(user);
//        when(user.id()).thenReturn(userId);
//        when(user.username()).thenReturn("test-user");
//        when(message.text()).thenReturn(text);
//
//        return update;
//    }
// }
