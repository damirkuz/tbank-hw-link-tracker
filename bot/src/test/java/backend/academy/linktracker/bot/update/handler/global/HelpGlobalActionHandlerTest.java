package backend.academy.linktracker.bot.update.handler.global;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.config.properties.CommandMessage;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.update.handler.command.CommandHandlerRegistry;
import com.pengrad.telegrambot.model.Update;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("HelpGlobalActionHandler")
class HelpGlobalActionHandlerTest {

    @Mock
    private BotOperations botOperations;

    @Mock
    private BotTextService botTextService;

    @Mock
    private CommandHandlerRegistry commandHandlerRegistry;

    private static final BotProperties.TagKeyboard DEFAULT_TAG_KB = new BotProperties.TagKeyboard(20, 8, 3, "", "", "");

    @Test
    @DisplayName("supports — true для команды /help")
    void supportsHelp() {
        BotProperties props = new BotProperties(Map.of(), DEFAULT_TAG_KB);
        HelpGlobalActionHandler handler =
                new HelpGlobalActionHandler(botOperations, botTextService, props, commandHandlerRegistry);
        UpdateContext ctx = mock(UpdateContext.class);
        when(ctx.isCallbackOrCommandAction("help")).thenReturn(true);
        assertThat(handler.supports(ctx)).isTrue();
    }

    @Test
    @DisplayName("Отправляет список команд с описанием")
    void shouldSendHelpWithCommands() {
        long chatId = 42L;
        Map<String, CommandMessage> messages = new LinkedHashMap<>();
        messages.put("track", new CommandMessage("/track", "Отслеживать ссылку", null));
        messages.put("list", new CommandMessage("/list", "Список ссылок", null));

        BotProperties props = new BotProperties(messages, DEFAULT_TAG_KB);
        HelpGlobalActionHandler handler =
                new HelpGlobalActionHandler(botOperations, botTextService, props, commandHandlerRegistry);

        UpdateContext ctx = mock(UpdateContext.class);
        when(ctx.chatId()).thenReturn(chatId);
        when(botTextService.get("bot.common.help")).thenReturn("Команды:");

        handler.handle(mock(Update.class), ctx);

        verify(botOperations).sendMessage(chatId, "Команды:\n/track - Отслеживать ссылку\n/list - Список ссылок");
    }

    @Test
    @DisplayName("Ничего не делает если chatId == null")
    void shouldDoNothingWhenChatIdNull() {
        BotProperties props = new BotProperties(Map.of(), DEFAULT_TAG_KB);
        HelpGlobalActionHandler handler =
                new HelpGlobalActionHandler(botOperations, botTextService, props, commandHandlerRegistry);
        UpdateContext ctx = mock(UpdateContext.class);
        when(ctx.chatId()).thenReturn(null);

        handler.handle(mock(Update.class), ctx);

        verify(botOperations, never()).sendMessage(anyLong(), anyString());
    }
}
