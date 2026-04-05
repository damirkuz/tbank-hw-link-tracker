package backend.academy.linktracker.bot.update.router;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.config.properties.TelegramProperties;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.update.handler.UnknownUpdateHandler;
import backend.academy.linktracker.bot.update.handler.command.CommandHandler;
import backend.academy.linktracker.bot.update.handler.command.CommandHandlerRegistry;
import com.pengrad.telegrambot.model.Update;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommandRouter")
class CommandRouterTest {

    @Mock
    private CommandHandlerRegistry commandHandlerRegistry;

    @Mock
    private UnknownUpdateHandler unknownUpdateHandler;

    @Mock
    private CommandHandler commandHandler;

    private CommandRouter commandRouter;

    @BeforeEach
    void setUp() {
        commandRouter = new CommandRouter(
                commandHandlerRegistry,
                new TelegramProperties("https://example.com", "token", "mybot", null, false),
                unknownUpdateHandler);
    }

    @Test
    @DisplayName("Находит handler и вызывает его")
    void shouldFindHandlerAndDelegate() {
        Update update = mock(Update.class);
        UpdateContext ctx = new UpdateContext(1, 1L, 1L, "/start", null);
        when(commandHandlerRegistry.findByCommandText("/start")).thenReturn(Optional.of(commandHandler));

        commandRouter.route(update, ctx);

        verify(commandHandler).handle(update, ctx);
        verifyNoInteractions(unknownUpdateHandler);
    }

    @Test
    @DisplayName("Вызывает unknownUpdateHandler если handler не найден")
    void shouldCallUnknownHandlerWhenNotFound() {
        Update update = mock(Update.class);
        UpdateContext ctx = new UpdateContext(1, 1L, 1L, "/unknown", null);
        when(commandHandlerRegistry.findByCommandText("/unknown")).thenReturn(Optional.empty());

        commandRouter.route(update, ctx);

        verify(unknownUpdateHandler).handle(update, ctx);
        verifyNoInteractions(commandHandler);
    }

    @Test
    @DisplayName("Вызывает unknownUpdateHandler если команда не для этого бота")
    void shouldCallUnknownHandlerWhenCommandNotForThisBot() {
        Update update = mock(Update.class);
        UpdateContext ctx = new UpdateContext(1, 1L, 1L, "/start@otherbot", null);

        commandRouter.route(update, ctx);

        verify(unknownUpdateHandler).handle(update, ctx);
        verifyNoInteractions(commandHandler, commandHandlerRegistry);
    }
}
