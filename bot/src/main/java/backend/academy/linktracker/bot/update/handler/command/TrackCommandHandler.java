package backend.academy.linktracker.bot.update.handler.command;

import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.usecase.DialogFlowService;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("/track")
@RequiredArgsConstructor
public class TrackCommandHandler implements CommandHandler {
    private final DialogFlowService dialogFlowService;

    @Override
    public void handle(Update update, UpdateContext updateContext) {
        dialogFlowService.startTrackFlow(updateContext);
    }
}
