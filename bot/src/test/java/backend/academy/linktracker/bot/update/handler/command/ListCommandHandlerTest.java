package backend.academy.linktracker.bot.update.handler.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.contracts.dto.response.CommonLinkResponse;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListCommandHandler")
class ListCommandHandlerTest {

    @Mock
    private ScrapperGateway scrapperClient;

    @Mock
    private BotTextService botTextService;

    @Mock
    private BotOperations botOperations;

    @InjectMocks
    private ListCommandHandler handler;

    @Test
    @DisplayName("Отправляет список ссылок если они есть")
    void shouldSendLinkList() {
        long chatId = 1L;
        Update update = mockUpdate(chatId, "/list");
        UpdateContext ctx = mockCtx(chatId);

        CommonLinkResponse link = new CommonLinkResponse(1L, URI.create("https://github.com"), List.of("java"), null);
        CommonListLinksResponse response = new CommonListLinksResponse(List.of(link), 1);
        when(scrapperClient.getLinks(chatId)).thenReturn(response);
        when(botTextService.get("bot.list.link-list")).thenReturn("Ваши ссылки:");
        when(botTextService.get("bot.list.link-in-list", 1L, URI.create("https://github.com")))
                .thenReturn("1. https://github.com");
        when(botTextService.get("bot.list.link-in-list-tags", "java")).thenReturn("[java]");

        handler.handle(update, ctx);

        verify(botOperations).sendMessage(chatId, "Ваши ссылки:\n1. https://github.com [java]");
    }

    @Test
    @DisplayName("Отправляет сообщение о пустом списке если ссылок нет")
    void shouldSendEmptyMessage() {
        long chatId = 1L;
        Update update = mockUpdate(chatId, "/list");
        UpdateContext ctx = mockCtx(chatId);

        when(scrapperClient.getLinks(chatId)).thenReturn(new CommonListLinksResponse(List.of(), 0));
        when(botTextService.get("bot.list.empty-link-list")).thenReturn("Список пуст.");

        handler.handle(update, ctx);

        verify(botOperations).sendMessage(chatId, "Список пуст.");
    }

    @Test
    @DisplayName("Регистрирует чат если ChatNotFoundException и повторяет запрос")
    void shouldRegisterChatOnNotFound() {
        long chatId = 1L;
        Update update = mockUpdate(chatId, "/list");
        UpdateContext ctx = mockCtx(chatId);

        when(scrapperClient.getLinks(chatId))
                .thenThrow(new ChatNotFoundException("not found"))
                .thenReturn(new CommonListLinksResponse(List.of(), 0));
        when(botTextService.get("bot.list.empty-link-list")).thenReturn("Список пуст.");

        handler.handle(update, ctx);

        verify(scrapperClient).registerChat(chatId);
        verify(botOperations).sendMessage(chatId, "Список пуст.");
    }

    @Test
    @DisplayName("Фильтрует ссылки по тегу из аргумента команды")
    void shouldFilterByTag() {
        long chatId = 1L;
        Update update = mockUpdate(chatId, "/list java");
        UpdateContext ctx = mockCtx(chatId);

        CommonLinkResponse javaLink =
                new CommonLinkResponse(1L, URI.create("https://github.com"), List.of("java"), null);
        CommonLinkResponse otherLink =
                new CommonLinkResponse(2L, URI.create("https://example.com"), List.of("other"), null);
        when(scrapperClient.getLinks(chatId)).thenReturn(new CommonListLinksResponse(List.of(javaLink, otherLink), 2));
        when(botTextService.get("bot.list.link-list")).thenReturn("Ваши ссылки:");
        when(botTextService.get("bot.list.link-in-list", 1L, URI.create("https://github.com")))
                .thenReturn("1. https://github.com");
        when(botTextService.get("bot.list.link-in-list-tags", "java")).thenReturn("[java]");

        handler.handle(update, ctx);

        verify(botOperations).sendMessage(chatId, "Ваши ссылки:\n1. https://github.com [java]");
    }

    private Update mockUpdate(long chatId, String text) {
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
