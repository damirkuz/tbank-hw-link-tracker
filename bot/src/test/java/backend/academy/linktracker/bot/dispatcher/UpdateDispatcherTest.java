// package backend.academy.linktracker.bot.dispatcher;
//
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.verifyNoInteractions;
// import static org.mockito.Mockito.when;
//
// import backend.academy.linktracker.bot.handler.command.CommandHandler;
// import backend.academy.linktracker.bot.handler.command.CommandHandlerRegistry;
// import backend.academy.linktracker.bot.model.UserState;
// import backend.academy.linktracker.bot.repository.StateRepository;
// import backend.academy.linktracker.bot.router.CommandRouter;
// import backend.academy.linktracker.bot.router.IdleRouter;
// import backend.academy.linktracker.bot.router.StateRouter;
// import backend.academy.linktracker.bot.util.StringParser;
// import com.pengrad.telegrambot.model.Chat;
// import com.pengrad.telegrambot.model.Message;
// import com.pengrad.telegrambot.model.Update;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.test.util.ReflectionTestUtils;
//
// @ExtendWith(MockitoExtension.class)
// @DisplayName("UpdateDispatcher: общая логика маршрутизации Update'ов")
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
//    private StateRepository stateRepository;
//
//    @Mock
//    private CommandHandlerRegistry commandHandlers;
//
//    @Mock
//    private StringParser parser;
//
//    @Mock
//    private CommandHandler commandHandler;
//
//    @InjectMocks
//    private UpdateDispatcher updateDispatcher;
//
//    private Update createUpdateMock(Long chatId, String text) {
//        Update update = new Update();
//        if (chatId != null || text != null) {
//            Message message = new Message();
//            if (chatId != null) {
//                Chat chat = new Chat();
//                ReflectionTestUtils.setField(chat, "id", chatId);
//                ReflectionTestUtils.setField(message, "chat", chat);
//            }
//            if (text != null) {
//                ReflectionTestUtils.setField(message, "text", text);
//            }
//            ReflectionTestUtils.setField(update, "message", message);
//        }
//        return update;
//    }
//
//    @Test
//    @DisplayName("Негативный сценарий: Update без сообщения (message == null) игнорируется")
//    void shouldIgnoreUpdateWhenMessageIsNull() {
//        // Создаем пустой апдейт (например, прилетел callback_query или edited_message)
//        Update update = new Update();
//
//        updateDispatcher.dispatch(update);
//
//        verifyNoInteractions(commandRouter, stateRouter, idleRouter, stateRepository);
//    }
//
//    @Test
//    @DisplayName("Сценарий отмены: Известная команда отмены сбрасывает состояние и идет в CommandRouter")
//    void shouldCancelStateOnCancelCommand() {
//        long chatId = 1L;
//        Update update = createUpdateMock(chatId, "/start");
//
//        when(parser.parseCommand("/start")).thenReturn("/start");
//        when(stateRepository.getUserState(chatId)).thenReturn(UserState.AUTH_WAIT_LINK);
//        when(commandHandlers.getHandler("/start")).thenReturn(commandHandler);
//        when(commandHandler.isCancelStateCommand()).thenReturn(true);
//
//        updateDispatcher.dispatch(update);
//
//        // Используем any(), так как класс Update не переопределяет equals()
//        verify(commandRouter).route(any(Update.class), any(String.class));
//        verify(stateRepository).setUserState(chatId, UserState.IDLE);
//        verifyNoInteractions(stateRouter, idleRouter);
//    }
//
//    @Test
//    @DisplayName("Обычная команда в состоянии IDLE уходит в CommandRouter")
//    void shouldRouteToCommandRouterWhenIdle() {
//        long chatId = 2L;
//        Update update = createUpdateMock(chatId, "/help");
//
//        when(parser.parseCommand("/help")).thenReturn("/help");
//        when(stateRepository.getUserState(chatId)).thenReturn(UserState.IDLE);
//        when(commandHandlers.getHandler("/help")).thenReturn(commandHandler);
//        when(commandHandler.isCancelStateCommand()).thenReturn(false);
//
//        updateDispatcher.dispatch(update);
//
//        verify(commandRouter).route(any(Update.class), any(String.class));
//        verifyNoInteractions(stateRouter, idleRouter);
//    }
//
//    @Test
//    @DisplayName("При активном состоянии (не IDLE) и вводе текста маршрутизация уходит в StateRouter")
//    void shouldRouteToStateRouterWhenNotIdle() {
//        long chatId = 3L;
//        Update update = createUpdateMock(chatId, "https://example.com");
//
//        when(stateRepository.getUserState(chatId)).thenReturn(UserState.AUTH_WAIT_LINK);
//        // Убрали when(parser.parseCommand...), чтобы избежать UnnecessaryStubbingException
//
//        updateDispatcher.dispatch(update);
//
//        verify(stateRouter).route(any(Update.class));
//        verifyNoInteractions(commandRouter, idleRouter);
//    }
//
//    @Test
//    @DisplayName("Неизвестный текст в состоянии IDLE уходит в IdleRouter")
//    void shouldRouteToIdleRouterWhenUnknownText() {
//        long chatId = 4L;
//        Update update = createUpdateMock(chatId, "Просто текст");
//
//        when(stateRepository.getUserState(chatId)).thenReturn(UserState.IDLE);
//        // Убрали when(parser.parseCommand...), чтобы избежать UnnecessaryStubbingException
//
//        updateDispatcher.dispatch(update);
//
//        verify(idleRouter).route(any(Update.class));
//        verifyNoInteractions(commandRouter, stateRouter);
//    }
// }
