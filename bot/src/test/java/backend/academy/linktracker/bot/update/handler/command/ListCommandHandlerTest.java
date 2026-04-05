package backend.academy.linktracker.bot.update.handler.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.LinkListService;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.service.keyboard.TagKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
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
    private BotOperations botOperations;

    @Mock
    private LinkListService linkListService;

    @Mock
    private TagKeyboardFactory tagKeyboardFactory;

    @Mock
    private ReplyKeyboardFactory replyKeyboardFactory;

    @Mock
    private BotProperties botProperties;

    @InjectMocks
    private ListCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(botProperties.commandOf("list-command")).thenReturn("/list");
    }

    @Test
    @DisplayName("Отправляет список с клавиатурой тегов если теги есть")
    void shouldSendListWithTagKeyboard() {
        long chatId = 1L;
        UpdateContext ctx = mockCtx(chatId);
        Update update = mockUpdate("/list");
        Set<String> tags = Set.of("java", "spring");
        InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);

        when(linkListService.buildListMessage(chatId, "")).thenReturn("Ваши ссылки:");
        when(linkListService.collectTags(chatId)).thenReturn(tags);
        when(tagKeyboardFactory.tagFilter(tags)).thenReturn(keyboard);

        handler.handle(update, ctx);

        verify(botOperations).sendMessage(chatId, "Ваши ссылки:", keyboard);
    }

    @Test
    @DisplayName("Отправляет список без клавиатуры если теги пустые")
    void shouldSendListWithoutKeyboardWhenNoTags() {
        long chatId = 1L;
        UpdateContext ctx = mockCtx(chatId);
        Update update = mockUpdate("/list");

        when(linkListService.buildListMessage(chatId, "")).thenReturn("Список пуст.");
        when(linkListService.collectTags(chatId)).thenReturn(Set.of());

        handler.handle(update, ctx);

        verify(botOperations).sendMessage(chatId, "Список пуст.");
    }

    @Test
    @DisplayName("Фильтрует по тегу из аргумента команды")
    void shouldFilterByTag() {
        long chatId = 1L;
        UpdateContext ctx = mockCtx(chatId);
        Update update = mockUpdate("/list java");

        when(linkListService.buildListMessage(chatId, "java")).thenReturn("Ссылки [java]:");

        handler.handle(update, ctx);

        verify(linkListService).buildListMessage(chatId, "java");
        verify(botOperations).sendMessage(chatId, "Ссылки [java]:");
    }

    @Test
    @DisplayName("Не использует текст алиаса как тег")
    void shouldNotUseAliasTextAsTag() {
        long chatId = 1L;
        UpdateContext ctx = mockCtx(chatId);
        Update update = mockUpdate("📋 Мои ссылки");
        Set<String> tags = Set.of("java");
        InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);

        when(linkListService.buildListMessage(chatId, "")).thenReturn("Ваши ссылки:");
        when(linkListService.collectTags(chatId)).thenReturn(tags);
        when(tagKeyboardFactory.tagFilter(tags)).thenReturn(keyboard);

        handler.handle(update, ctx);

        verify(linkListService).buildListMessage(chatId, "");
        verify(botOperations).sendMessage(chatId, "Ваши ссылки:", keyboard);
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
