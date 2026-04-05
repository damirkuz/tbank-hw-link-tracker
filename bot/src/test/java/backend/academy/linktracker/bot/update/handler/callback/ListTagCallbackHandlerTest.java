package backend.academy.linktracker.bot.update.handler.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.LinkListService;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListTagCallbackHandler")
class ListTagCallbackHandlerTest {

    @Mock
    private BotOperations botOperations;

    @Mock
    private LinkListService linkListService;

    @Mock
    private ReplyKeyboardFactory replyKeyboardFactory;

    @Mock
    private BotProperties botProperties;

    @InjectMocks
    private ListTagCallbackHandler handler;

    private static final BotProperties.TagKeyboard TAG_KB =
            new BotProperties.TagKeyboard(20, 8, 3, "list_tag:", "list_tags_all", "list_tag_input");

    @BeforeEach
    void setUp() {
        when(botProperties.tagKeyboard()).thenReturn(TAG_KB);
    }

    @Test
    @DisplayName("supports — true для list_tag:*")
    void supportsListTag() {
        assertThat(handler.supports("list_tag:java")).isTrue();
    }

    @Test
    @DisplayName("supports — false для других")
    void supportsOther() {
        assertThat(handler.supports("list_tags_all")).isFalse();
        assertThat(handler.supports(null)).isFalse();
    }

    @Test
    @DisplayName("Отправляет отфильтрованный список с главным меню")
    void shouldSendFilteredList() {
        long chatId = 1L;
        UpdateContext ctx = mock(UpdateContext.class);
        when(ctx.chatId()).thenReturn(chatId);
        when(ctx.callbackData()).thenReturn("list_tag:java");
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(replyKeyboardFactory.mainMenu()).thenReturn(keyboard);
        when(linkListService.buildListMessage(chatId, "java")).thenReturn("Ссылки [java]:");

        handler.handle(mock(Update.class), ctx);

        verify(botOperations).sendMessage(chatId, "Ссылки [java]:", keyboard);
    }
}
