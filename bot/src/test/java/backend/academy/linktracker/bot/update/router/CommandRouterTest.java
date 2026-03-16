package backend.academy.linktracker.bot.update.router;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.update.handler.command.CommandHandler;
import backend.academy.linktracker.bot.update.handler.command.CommandHandlerRegistry;
import backend.academy.linktracker.bot.util.StringParser;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommandRouter")
class CommandRouterTest {

    @Mock
    private CommandHandlerRegistry commandHandlerRegistry;

    @Mock
    private StringParser parser;

    @Mock
    private CommandHandler commandHandler;

    @InjectMocks
    private CommandRouter commandRouter;

    @Test
    @DisplayName("Парсит команду и делегирует update найденному handler")
    void shouldParseCommandAndDelegateToResolvedHandler() {
        String rawText = "/start param";
        String parsedCommand = "/start";
        Update update = createUpdate(rawText);

        when(parser.parseCommand(rawText)).thenReturn(parsedCommand);
        when(commandHandlerRegistry.findByCommandText(parsedCommand)).thenReturn(Optional.of(commandHandler));

        commandRouter.route(update);

        verify(parser).parseCommand(rawText);
        verify(commandHandlerRegistry).findByCommandText(parsedCommand);
        verify(commandHandler).handle(same(update));
    }

    @Test
    @DisplayName("Ничего не делает, если handler не найден")
    void shouldDoNothingWhenHandlerNotFound() {
        String rawText = "/unknown";
        String parsedCommand = "/unknown";
        Update update = createUpdate(rawText);

        when(parser.parseCommand(rawText)).thenReturn(parsedCommand);
        when(commandHandlerRegistry.findByCommandText(parsedCommand)).thenReturn(Optional.empty());

        commandRouter.route(update);

        verify(parser).parseCommand(rawText);
        verify(commandHandlerRegistry).findByCommandText(parsedCommand);
        verifyNoInteractions(commandHandler);
    }

    @Test
    @DisplayName("Прямой route(update, handler) вызывает переданный handler")
    void shouldCallProvidedHandlerDirectly() {
        Update update = mock(Update.class);

        commandRouter.route(update, commandHandler);

        verify(commandHandler).handle(same(update));
        verifyNoInteractions(commandHandlerRegistry, parser);
    }

    private Update createUpdate(String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);

        return update;
    }
}
