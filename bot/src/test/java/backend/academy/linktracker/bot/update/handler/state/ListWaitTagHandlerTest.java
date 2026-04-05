package backend.academy.linktracker.bot.update.handler.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.LinkListService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListWaitTagHandler")
class ListWaitTagHandlerTest {

    @Mock
    private LinkListService linkListService;

    @Mock
    private BotOperations botOperations;

    @Mock
    private StateStorage stateStorage;

    @Mock
    private ReplyKeyboardFactory replyKeyboardFactory;

    @InjectMocks
    private ListWaitTagHandler handler;

    @Test
    @DisplayName("getHandledState — LIST_WAIT_TAG")
    void handledState() {
        assertThat(handler.getHandledState()).isEqualTo(UserState.LIST_WAIT_TAG);
    }

    @Test
    @DisplayName("Сбрасывает состояние, фильтрует по введённому тегу, возвращает меню")
    void shouldResetStateAndSendFilteredList() {
        long chatId = 1L;
        long userId = 2L;
        UpdateContext ctx = mock(UpdateContext.class);
        when(ctx.chatId()).thenReturn(chatId);
        when(ctx.userId()).thenReturn(userId);

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("  Java  ");

        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(replyKeyboardFactory.mainMenu()).thenReturn(keyboard);
        when(linkListService.buildListMessage(chatId, "java")).thenReturn("Ссылки [java]:");

        handler.handle(update, ctx);

        verify(stateStorage).updateState(userId, UserState.IDLE);
        verify(botOperations).sendMessage(chatId, "Ссылки [java]:", keyboard);
    }

    @Test
    @DisplayName("Приводит тег к нижнему регистру перед фильтрацией")
    void shouldNormalizeTagToLowercase() {
        long chatId = 1L;
        long userId = 2L;
        UpdateContext ctx = mock(UpdateContext.class);
        when(ctx.chatId()).thenReturn(chatId);
        when(ctx.userId()).thenReturn(userId);

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("SPRING");

        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(replyKeyboardFactory.mainMenu()).thenReturn(keyboard);
        when(linkListService.buildListMessage(chatId, "spring")).thenReturn("Ссылки [spring]:");

        handler.handle(update, ctx);

        verify(linkListService).buildListMessage(chatId, "spring");
    }
}
