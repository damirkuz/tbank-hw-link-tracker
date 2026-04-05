package backend.academy.linktracker.bot.update.handler.state;

import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.usecase.UntrackFlowService;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UntrackWaitLinkHandler implements StateHandler {

    private final UntrackFlowService untrackFlowService;

    @Override
    public UserState getHandledState() {
        return UserState.UNTRACK_WAIT_LINK;
    }

    @Override
    public void handle(Update update, UpdateContext updateContext) {
        untrackFlowService.handleLinkInput(updateContext, update.message().text());
    }
}
