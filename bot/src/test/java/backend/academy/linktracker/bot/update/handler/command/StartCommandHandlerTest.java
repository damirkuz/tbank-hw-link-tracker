// package backend.academy.linktracker.bot.update.handler.command;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
// import static org.mockito.Mockito.doThrow;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
//
// import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
// import backend.academy.linktracker.bot.service.BotOperations;
// import backend.academy.linktracker.bot.service.BotTextService;
// import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
// import com.pengrad.telegrambot.model.Chat;
// import com.pengrad.telegrambot.model.Message;
// import com.pengrad.telegrambot.model.Update;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
//
// @ExtendWith(MockitoExtension.class)
// @DisplayName("StartCommandHandler")
// class StartCommandHandlerTest {
//
//    @Mock
//    private ScrapperGateway scrapperClient;
//
//    @Mock
//    private BotOperations botOperations;
//
//    @Mock
//    private BotTextService botTextService;
//
//    @InjectMocks
//    private StartCommandHandler handler;
//
//    @Test
//    @DisplayName("Отправляет стартовый текст и регистрирует чат")
//    void shouldSendStartTextAndRegisterChat() {
//        long chatId = 123L;
//        String startText = "Привет! Это стартовое сообщение.";
//        Update update = createUpdate(chatId);
//
//        when(botTextService.get("bot.common.start")).thenReturn(startText);
//
//        handler.handle(update);
//
//        verify(botTextService).get("bot.common.start");
//        verify(botOperations).sendMessage(chatId, startText);
//        verify(scrapperClient).registerChat(chatId);
//    }
//
//    @Test
//    @DisplayName("Не падает, если чат уже зарегистрирован")
//    void shouldIgnoreChatAlreadyExistsException() {
//        long chatId = 123L;
//        String startText = "Привет! Это стартовое сообщение.";
//        Update update = createUpdate(chatId);
//
//        when(botTextService.get("bot.common.start")).thenReturn(startText);
//        doThrow(mock(ChatAlreadyExistsException.class)).when(scrapperClient).registerChat(chatId);
//
//        assertDoesNotThrow(() -> handler.handle(update));
//
//        verify(botTextService).get("bot.common.start");
//        verify(botOperations).sendMessage(chatId, startText);
//        verify(scrapperClient).registerChat(chatId);
//    }
//
//    @Test
//    @DisplayName("Помечен как cancel-state команда")
//    void shouldBeCancelStateCommand() {
//        assertThat(handler.isCancelStateCommand()).isTrue();
//    }
//
//    private Update createUpdate(long chatId) {
//        Update update = mock(Update.class);
//        Message message = mock(Message.class);
//        Chat chat = mock(Chat.class);
//
//        when(update.message()).thenReturn(message);
//        when(message.chat()).thenReturn(chat);
//        when(chat.id()).thenReturn(chatId);
//
//        return update;
//    }
// }
