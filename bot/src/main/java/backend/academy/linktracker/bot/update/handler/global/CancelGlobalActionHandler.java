package backend.academy.linktracker.bot.update.handler.global;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.usecase.DialogFlowService;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelGlobalActionHandler implements GlobalActionHandler {

    private final DialogFlowService dialogFlowService;
    private final BotProperties botProperties;

    @Override
    public boolean supports(UpdateContext context) {
        return context.isCallbackOrCommandAction("cancel")
                || botProperties.isAliasForCommand("/cancel", context.messageText());
    }

    @Override
    public void handle(Update update, UpdateContext context) {
        if (context.chatId() == null || context.userId() == null) {
            return;
        }

        dialogFlowService.cancelConversation(context);
    }
}
