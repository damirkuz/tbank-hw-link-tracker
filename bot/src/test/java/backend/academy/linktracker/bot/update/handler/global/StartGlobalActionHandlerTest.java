package backend.academy.linktracker.bot.update.handler.global;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.bot.model.UserChatKey;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StartGlobalActionHandler")
class StartGlobalActionHandlerTest {

    @Mock
    private BotOperations botOperations;

    @Mock
    private BotTextService botTextService;

    @Mock
    private ScrapperGateway scrapperClient;

    @Mock
    private StateStorage stateStorage;

    @Mock
    private ReplyKeyboardFactory replyKeyboardFactory;

    @InjectMocks
    private StartGlobalActionHandler handler;

    @Test
    @DisplayName("supports — true для команды /start")
    void supportsStartCommand() {
        UpdateContext ctx = new UpdateContext(1, 1L, 1L, "/start", null);
        assertThat(handler.supports(ctx)).isTrue();
    }

    @Test
    @DisplayName("supports — false для другой команды")
    void supportsOther() {
        UpdateContext ctx = new UpdateContext(1, 1L, 1L, "/help", null);
        assertThat(handler.supports(ctx)).isFalse();
    }

    @Test
    @DisplayName("Сбрасывает состояние, отправляет текст и регистрирует чат")
    void shouldClearStateAndRegisterChat() {
        long chatId = 1L;
        long userId = 2L;
        UpdateContext ctx = mockCtx(chatId, userId);
        when(botTextService.get("bot.common.start")).thenReturn("Привет!");
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(replyKeyboardFactory.mainMenu()).thenReturn(keyboard);

        handler.handle(mockUpdate(), ctx);

        verify(stateStorage).clearState(new UserChatKey(userId, chatId));
        verify(botOperations).sendMessage(chatId, "Привет!", keyboard);
        verify(scrapperClient).registerChat(chatId);
    }

    @Test
    @DisplayName("Не падает при ChatAlreadyExistsException")
    void shouldIgnoreChatAlreadyExists() {
        long chatId = 1L;
        long userId = 2L;
        UpdateContext ctx = mockCtx(chatId, userId);
        when(botTextService.get("bot.common.start")).thenReturn("Привет!");
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(replyKeyboardFactory.mainMenu()).thenReturn(keyboard);
        doThrow(mock(ChatAlreadyExistsException.class)).when(scrapperClient).registerChat(chatId);

        handler.handle(mockUpdate(), ctx);

        verify(botOperations).sendMessage(chatId, "Привет!", keyboard);
    }

    @Test
    @DisplayName("Ничего не делает если chatId == null")
    void shouldDoNothingWhenChatIdNull() {
        UpdateContext ctx = new UpdateContext(1, null, 2L, "/start", null);

        handler.handle(mock(Update.class), ctx);

        verify(botOperations, never()).sendMessage(anyLong(), anyString(), any());
    }

    private Update mockUpdate() {
        return mock(Update.class);
    }

    private UpdateContext mockCtx(long chatId, long userId) {
        return new UpdateContext(1, chatId, userId, "/start", null);
    }
}
