package backend.academy.linktracker.bot.update.handler.command;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.validator.link.LinkValidationResult;
import backend.academy.linktracker.bot.validator.link.LinkValidationService;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UntrackCommandHandler")
class UntrackCommandHandlerTest {

    @Mock
    private ScrapperGateway scrapperClient;

    @Mock
    private BotTextService botTextService;

    @Mock
    private BotOperations botOperations;

    @Mock
    private LinkValidationService linkValidationService;

    @InjectMocks
    private UntrackCommandHandler handler;

    private static final String VALID_LINK = "https://github.com/user/repo";

    @Test
    @DisplayName("Успешно удаляет ссылку")
    void shouldDeleteLinkSuccessfully() {
        long chatId = 1L;
        Update update = mockUpdate("/untrack " + VALID_LINK);
        UpdateContext ctx = mockCtx(chatId);

        when(linkValidationService.validate(VALID_LINK)).thenReturn(new LinkValidationResult(true, null, null));
        when(botTextService.get("bot.untrack.success")).thenReturn("Ссылка удалена.");

        handler.handle(update, ctx);

        verify(scrapperClient).deleteLink(chatId, new CommonRemoveLinkRequest(URI.create(VALID_LINK)));
        verify(botOperations).sendMessage(chatId, "Ссылка удалена.");
    }

    @Test
    @DisplayName("Отправляет ошибку если ссылка невалидна")
    void shouldSendErrorForInvalidLink() {
        long chatId = 1L;
        Update update = mockUpdate("/untrack not-a-link");
        UpdateContext ctx = mockCtx(chatId);

        when(linkValidationService.validate("not-a-link")).thenReturn(new LinkValidationResult(false, "invalid", null));
        when(botTextService.get("bot.track.invalid-link")).thenReturn("Некорректная ссылка.");

        handler.handle(update, ctx);

        verify(botOperations).sendMessage(chatId, "Некорректная ссылка.");
        verify(scrapperClient, never()).deleteLink(chatId, new CommonRemoveLinkRequest(URI.create("not-a-link")));
    }

    @Test
    @DisplayName("Отправляет ошибку если чат или ссылка не найдены")
    void shouldSendErrorOnChatNotFound() {
        long chatId = 1L;
        Update update = mockUpdate("/untrack " + VALID_LINK);
        UpdateContext ctx = mockCtx(chatId);

        when(linkValidationService.validate(VALID_LINK)).thenReturn(new LinkValidationResult(true, null, null));
        doThrow(new ChatNotFoundException("not found"))
                .when(scrapperClient)
                .deleteLink(chatId, new CommonRemoveLinkRequest(URI.create(VALID_LINK)));
        when(botTextService.get("bot.untrack.chat-or-link-not-found")).thenReturn("Не найдено.");

        handler.handle(update, ctx);

        verify(botOperations).sendMessage(chatId, "Не найдено.");
    }

    private Update mockUpdate(String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        return update;
    }

    private UpdateContext mockCtx(long chatId) {
        UpdateContext ctx = mock(UpdateContext.class);
        when(ctx.chatId()).thenReturn(chatId);
        return ctx;
    }
}
