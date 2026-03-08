package backend.academy.linktracker.bot.handler.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.properties.BotProperties;
import backend.academy.linktracker.bot.properties.message.CommandMessage;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("HelpCommandHandler")
class HelpCommandHandlerTest {

    @Mock
    private BotTextService botTextService;

    @Mock
    private BotOperations botOperations;

    @Test
    @DisplayName("Отправляет help-текст со списком команд")
    void shouldSendHelpMessageWithCommandsDescriptions() {
        long chatId = 456L;

        CommandMessage startCommand = new CommandMessage("/start", "Запустить бота");
        CommandMessage helpCommand = new CommandMessage("/help", "Показать помощь");

        Map<String, CommandMessage> messages = new LinkedHashMap<>();
        messages.put("start", startCommand);
        messages.put("help", helpCommand);

        BotProperties botProperties = new BotProperties(messages);
        HelpCommandHandler handler = new HelpCommandHandler(botTextService, botOperations, botProperties);

        when(botTextService.get("bot.common.help")).thenReturn("Доступные команды:");

        handler.handle(createUpdate(chatId));

        verify(botOperations)
                .sendMessage(chatId, "Доступные команды:\n/start - Запустить бота\n/help - Показать помощь");
    }

    @Test
    @DisplayName("Команда не является cancel-state командой")
    void shouldNotBeCancelStateCommand() {
        HelpCommandHandler handler = new HelpCommandHandler(botTextService, botOperations, new BotProperties(Map.of()));

        assertThat(handler.isCancelStateCommand()).isFalse();
    }

    private Update createUpdate(long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        return update;
    }
}
