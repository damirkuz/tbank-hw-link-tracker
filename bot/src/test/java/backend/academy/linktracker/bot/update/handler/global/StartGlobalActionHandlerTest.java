package backend.academy.linktracker.bot.update.handler.global;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
import com.pengrad.telegrambot.model.Update;
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

    @InjectMocks
    private StartGlobalActionHandler handler;

    @Test
    @DisplayName("supports — true для команды /start")
    void supportsStartCommand() {
        UpdateContext ctx = mock(UpdateContext.class);
        when(ctx.isCallbackOrCommandAction("start")).thenReturn(true);
        assertThat(handler.supports(ctx)).isTrue();
    }

    @Test
    @DisplayName("supports — false для другой команды")
    void supportsOther() {
        UpdateContext ctx = mock(UpdateContext.class);
        when(ctx.isCallbackOrCommandAction("start")).thenReturn(false);
        assertThat(handler.supports(ctx)).isFalse();
    }

    @Test
    @DisplayName("Сбрасывает состояние, отправляет текст и регистрирует чат")
    void shouldClearStateAndRegisterChat() {
        long chatId = 1L;
        long userId = 2L;
        Update update = mockUpdate(chatId);
        UpdateContext ctx = mockCtx(chatId, userId);
        when(botTextService.get("bot.common.start")).thenReturn("Привет!");

        handler.handle(update, ctx);

        verify(stateStorage).clearState(userId);
        verify(botOperations).sendMessage(chatId, "Привет!");
        verify(scrapperClient).registerChat(chatId);
    }

    @Test
    @DisplayName("Не падает при ChatAlreadyExistsException")
    void shouldIgnoreChatAlreadyExists() {
        long chatId = 1L;
        long userId = 2L;
        Update update = mockUpdate(chatId);
        UpdateContext ctx = mockCtx(chatId, userId);
        when(botTextService.get("bot.common.start")).thenReturn("Привет!");
        doThrow(mock(ChatAlreadyExistsException.class)).when(scrapperClient).registerChat(chatId);

        handler.handle(update, ctx);

        verify(botOperations).sendMessage(chatId, "Привет!");
    }

    @Test
    @DisplayName("Ничего не делает если chatId == null")
    void shouldDoNothingWhenChatIdNull() {
        Update update = mock(Update.class);
        UpdateContext ctx = mock(UpdateContext.class);
        when(ctx.chatId()).thenReturn(null);

        handler.handle(update, ctx);

        // нет взаимодействий с botOperations
        verify(botOperations, org.mockito.Mockito.never())
                .sendMessage(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }

    private Update mockUpdate(long chatId) {
        Update update = mock(Update.class);
        return update;
    }

    private UpdateContext mockCtx(long chatId, long userId) {
        UpdateContext ctx = mock(UpdateContext.class);
        when(ctx.chatId()).thenReturn(chatId);
        when(ctx.userId()).thenReturn(userId);
        return ctx;
    }
}
