package backend.academy.linktracker.bot.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.validator.link.LinkValidationResult;
import backend.academy.linktracker.bot.validator.link.LinkValidationService;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UntrackFlowService")
class UntrackFlowServiceTest {

    private final LinkValidationService linkValidationService = mock(LinkValidationService.class);
    private final BotTextService botTextService = mock(BotTextService.class);
    private final BotOperations botOperations = mock(BotOperations.class);
    private final StateStorage stateStorage = mock(StateStorage.class);
    private final ScrapperGateway scrapperGateway = mock(ScrapperGateway.class);

    private final UntrackFlowService service =
            new UntrackFlowService(linkValidationService, botTextService, botOperations, stateStorage, scrapperGateway);

    @Test
    @DisplayName("Невалидная ссылка не вызывает scrapper и не очищает state")
    void invalidLink() {
        UpdateContext context = new UpdateContext(1, 1L, 2L, null, null);
        when(linkValidationService.validate("bad")).thenReturn(new LinkValidationResult(false, "invalid", null));
        when(botTextService.get("bot.track.invalid-link")).thenReturn("Некорректная ссылка");

        service.handleLinkInput(context, "bad");

        verify(botOperations).sendMessage(1L, "Некорректная ссылка");
        verify(scrapperGateway, never()).deleteLink(eq(1L), any());
        verify(stateStorage, never()).clearState(new UserChatKey(2L, 1L));
    }

    @Test
    @DisplayName("Успешное удаление очищает state и отправляет success")
    void success() {
        UpdateContext context = new UpdateContext(1, 1L, 2L, null, null);
        String link = "https://github.com/user/repo";
        when(linkValidationService.validate(link)).thenReturn(new LinkValidationResult(true, null, null));
        when(botTextService.get("bot.untrack.success")).thenReturn("Удалено");

        service.handleLinkInput(context, link);

        verify(scrapperGateway).deleteLink(eq(1L), eq(new CommonRemoveLinkRequest(URI.create(link))));
        verify(stateStorage).clearState(new UserChatKey(2L, 1L));
        verify(botOperations).sendMessage(1L, "Удалено");
    }

    @Test
    @DisplayName("ChatNotFoundException возвращает not-found answer и очищает state")
    void notFound() {
        UpdateContext context = new UpdateContext(1, 1L, 2L, null, null);
        String link = "https://github.com/user/repo";
        when(linkValidationService.validate(link)).thenReturn(new LinkValidationResult(true, null, null));
        doThrow(new ChatNotFoundException("nf"))
                .when(scrapperGateway)
                .deleteLink(eq(1L), eq(new CommonRemoveLinkRequest(URI.create(link))));
        when(botTextService.get("bot.untrack.chat-or-link-not-found")).thenReturn("Не найдено");

        service.handleLinkInput(context, link);

        verify(stateStorage).clearState(new UserChatKey(2L, 1L));
        verify(botOperations).sendMessage(1L, "Не найдено");
    }
}
