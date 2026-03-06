//package backend.academy.linktracker.bot.router;
//
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import backend.academy.linktracker.bot.handler.command.CommandHandler;
//import backend.academy.linktracker.bot.handler.command.CommandHandlerRegistry;
//import backend.academy.linktracker.bot.util.StringParser;
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
//@DisplayName("CommandRouter: диспетчеризация команд")
//class CommandRouterTest {
//
//    @Mock
//    private CommandHandlerRegistry handlerRegistry;
//
//    @Mock
//    private StringParser parser;
//
//    @Mock
//    private CommandHandler commandHandler;
//
//    @InjectMocks
//    private CommandRouter commandRouter;
//
//    private Update createUpdateMock(String text) {
//        Update update = new Update();
//        Message message = new Message();
//
//        ReflectionTestUtils.setField(message, "text", text);
//        ReflectionTestUtils.setField(update, "message", message);
//
//        return update;
//    }
//
//    @Test
//    @DisplayName("Маршрутизатор корректно парсит команду и вызывает нужный хендлер")
//    void shouldRouteCommandToCorrectHandler() {
//        String rawText = "/start param";
//        String parsedCommand = "/start";
//        Update update = createUpdateMock(rawText);
//
//        when(parser.parseCommand(rawText)).thenReturn(parsedCommand);
//        when(handlerRegistry.getHandler(parsedCommand)).thenReturn(commandHandler);
//
//        commandRouter.route(update);
//
//        verify(parser, times(1)).parseCommand(rawText);
//        verify(handlerRegistry, times(1)).getHandler(parsedCommand);
//        verify(commandHandler, times(1)).handle(update);
//    }
//}
