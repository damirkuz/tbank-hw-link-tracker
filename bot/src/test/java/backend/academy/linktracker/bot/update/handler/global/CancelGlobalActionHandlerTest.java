package backend.academy.linktracker.bot.update.handler.global;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.config.properties.CommandMessage;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.usecase.DialogFlowService;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelGlobalActionHandler")
class CancelGlobalActionHandlerTest {

    @Mock
    private DialogFlowService dialogFlowService;

    private CancelGlobalActionHandler handler;

    @BeforeEach
    void setUp() {
        BotProperties botProperties = new BotProperties(
                java.util.Map.of("cancel", new CommandMessage("/cancel", "Cancel", java.util.List.of())),
                new BotProperties.TagKeyboard(20, 8, 3, "list_tag:", "list_tags_all", "list_tag_input"));
        handler = new CancelGlobalActionHandler(dialogFlowService, botProperties);
    }

    @Test
    @DisplayName("supports — true для команды /cancel")
    void supportsCancel() {
        UpdateContext ctx = new UpdateContext(1, 1L, 1L, "/cancel", null);
        assertThat(handler.supports(ctx)).isTrue();
    }

    @Test
    @DisplayName("Делегирует cancel-flow")
    void shouldDelegateToCancelFlow() {
        long chatId = 1L;
        long userId = 2L;
        UpdateContext ctx = mockCtx(chatId, userId);

        handler.handle(mock(Update.class), ctx);

        verify(dialogFlowService).cancelConversation(ctx);
    }

    @Test
    @DisplayName("Ничего не делает если chatId == null")
    void shouldDoNothingWhenChatIdNull() {
        UpdateContext ctx = new UpdateContext(1, null, 2L, "/cancel", null);

        handler.handle(mock(Update.class), ctx);

        verify(dialogFlowService, never()).cancelConversation(any());
    }

    private UpdateContext mockCtx(long chatId, long userId) {
        return new UpdateContext(1, chatId, userId, "/cancel", null);
    }
}
