package backend.academy.linktracker.bot.update.handler.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.model.UserChatKey;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListTagInputCallbackHandler")
class ListTagInputCallbackHandlerTest {

    @Mock
    private StateStorage stateStorage;

    @Mock
    private BotOperations botOperations;

    @Mock
    private BotTextService botTextService;

    @Mock
    private ReplyKeyboardFactory replyKeyboardFactory;

    private ListTagInputCallbackHandler handler;

    private static final BotProperties.TagKeyboard TAG_KB =
            new BotProperties.TagKeyboard(20, 8, 3, "list_tag:", "list_tags_all", "list_tag_input");

    @BeforeEach
    void setUp() {
        handler = new ListTagInputCallbackHandler(
                stateStorage,
                botOperations,
                botTextService,
                replyKeyboardFactory,
                new BotProperties(java.util.Map.of(), TAG_KB));
    }

    @Test
    @DisplayName("supports — true для list_tag_input")
    void supportsListTagInput() {
        assertThat(handler.supports("list_tag_input")).isTrue();
    }

    @Test
    @DisplayName("supports — false для других")
    void supportsOther() {
        assertThat(handler.supports("list_tag:java")).isFalse();
    }

    @Test
    @DisplayName("Устанавливает состояние LIST_WAIT_TAG и просит ввести тег")
    void shouldSetStateAndAskForTag() {
        long chatId = 1L;
        long userId = 2L;
        UpdateContext ctx = new UpdateContext(1, chatId, userId, null, "list_tag_input");
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(replyKeyboardFactory.cancelOnly()).thenReturn(keyboard);
        when(botTextService.get("bot.list.ask-tag")).thenReturn("Введи тег:");

        handler.handle(mock(Update.class), ctx);

        verify(stateStorage).updateState(new UserChatKey(userId, chatId), UserState.LIST_WAIT_TAG);
        verify(botOperations).sendMessage(chatId, "Введи тег:", keyboard);
    }
}
