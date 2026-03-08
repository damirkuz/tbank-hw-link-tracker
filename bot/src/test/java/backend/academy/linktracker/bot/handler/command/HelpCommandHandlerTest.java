// package backend.academy.linktracker.bot.handler.command;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
//
// import backend.academy.linktracker.bot.properties.message.CommandMessage;
// import backend.academy.linktracker.bot.properties.message.MessageProperties;
// import backend.academy.linktracker.bot.service.BotOperations;
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
// @DisplayName("HelpCommandHandler: проверка команды /help")
// class HelpCommandHandlerTest {
//
//    @Mock
//    private BotOperations botOperations;
//
//    @Mock
//    private MessageProperties messageProperties;
//
//    @InjectMocks
//    private HelpCommandHandler handler;
//
//    private Update createUpdateMock(long chatId) {
//        Update update = new Update();
//        Message message = new Message();
//        Chat chat = new Chat();
//
//        ReflectionTestUtils.setField(chat, "id", chatId);
//        ReflectionTestUtils.setField(message, "chat", chat);
//        ReflectionTestUtils.setField(update, "message", message);
//
//        return update;
//    }
//
//    @Test
//    @DisplayName("При /help бот отвечает списком команд из properties")
//    void shouldReturnHelpMessageOnHelp() {
//        long chatId = 456L;
//        String helpText = "Доступные команды:\n/start\n/help";
//        CommandMessage helpMsg = new CommandMessage("/help", "Показать помощь", helpText);
//
//        when(messageProperties.helpCommand()).thenReturn(helpMsg);
//
//        Update update = createUpdateMock(chatId);
//
//        handler.handle(update);
//
//        verify(botOperations, times(1)).sendMessage(chatId, helpText);
//    }
//
//    @Test
//    @DisplayName("Хендлер возвращает правильную команду и описание")
//    void shouldReturnCorrectCommandDetails() {
//        CommandMessage helpMsg = new CommandMessage("/help", "Показать помощь", "Любой текст");
//        when(messageProperties.helpCommand()).thenReturn(helpMsg);
//
//        assertThat(handler.getCommand()).isEqualTo("/help");
//        assertThat(handler.getDescription()).isEqualTo("Показать помощь");
//        assertThat(handler.isCancelStateCommand()).isFalse();
//    }
// }
