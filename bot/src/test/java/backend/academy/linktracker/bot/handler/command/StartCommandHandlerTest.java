//package backend.academy.linktracker.bot.handler.command;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import backend.academy.linktracker.bot.properties.message.CommandMessage;
//import backend.academy.linktracker.bot.properties.message.MessageProperties;
//import backend.academy.linktracker.bot.service.BotOperations;
//import com.pengrad.telegrambot.model.Chat;
//import com.pengrad.telegrambot.model.Message;
//import com.pengrad.telegrambot.model.Update;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("StartCommandHandler: проверка команды /start")
//class StartCommandHandlerTest {
//
//    @Mock
//    private MessageProperties messageProperties;
//
//    @Mock
//    private BotOperations botOperations;
//
//    @InjectMocks
//    private StartCommandHandler handler;
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
//    @DisplayName("При /start бот отправляет приветственное сообщение из properties")
//    void shouldReturnWelcomeMessageOnStart() {
//        long chatId = 123L;
//        String welcomeText = "Привет, напиши /help, чтобы посмотреть доступные команды";
//        CommandMessage startMsg = new CommandMessage("/start", "Запустить бота", welcomeText);
//
//        when(messageProperties.startCommand()).thenReturn(startMsg);
//
//        Update update = createUpdateMock(chatId);
//
//        handler.handle(update);
//
//        verify(botOperations, times(1)).sendMessage(chatId, welcomeText);
//    }
//
//    @Test
//    @DisplayName("Хендлер возвращает правильную команду и описание")
//    void shouldReturnCorrectCommandDetails() {
//        CommandMessage startMsg = new CommandMessage("/start", "Запустить бота", "Любой текст");
//        when(messageProperties.startCommand()).thenReturn(startMsg);
//
//        assertThat(handler.getCommand()).isEqualTo("/start");
//        assertThat(handler.getDescription()).isEqualTo("Запустить бота");
//        assertThat(handler.isCancelStateCommand()).isTrue();
//    }
//}
