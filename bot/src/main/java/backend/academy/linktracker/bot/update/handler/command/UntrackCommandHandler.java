package backend.academy.linktracker.bot.update.handler.command;

import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.usecase.DialogFlowService;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("/untrack")
@RequiredArgsConstructor
public class UntrackCommandHandler implements CommandHandler {

    private final DialogFlowService dialogFlowService;

    @Override
    public void handle(Update update, UpdateContext updateContext) {
        dialogFlowService.startUntrackFlow(updateContext);
    }
}
