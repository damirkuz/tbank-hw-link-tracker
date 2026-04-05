package backend.academy.linktracker.bot.update.handler.state;

import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.usecase.TrackFlowService;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackWaitTagsHandler implements StateHandler {

    private final TrackFlowService trackFlowService;

    @Override
    public UserState getHandledState() {
        return UserState.TRACK_WAIT_TAGS;
    }

    @Override
    public void handle(Update update, UpdateContext updateContext) {
        trackFlowService.handleTagsInput(updateContext, update.message().text());
    }
}
