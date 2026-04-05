package backend.academy.linktracker.bot.update.handler.state;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.bot.model.UserState;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.LinkListService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import com.pengrad.telegrambot.model.Update;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListWaitTagHandler implements StateHandler {

    private final ScrapperGateway scrapperClient;
    private final BotTextService botTextService;
    private final BotOperations botOperations;
    private final LinkListService linkListService;
    private final ReplyKeyboardFactory replyKeyboardFactory;
    private final StateStorage stateStorage;

    @Override
    public UserState getHandledState() {
        return UserState.LIST_WAIT_TAG;
    }

    @Override
    public void handle(Update update, UpdateContext context) {
        long chatId = context.chatId();
        String tag = update.message().text().trim().toLowerCase(Locale.ROOT);

        String answer = linkListService.buildListMessage(chatId, tag);
        stateStorage.updateState(context.userId(), UserState.IDLE);
        botOperations.sendMessage(chatId, answer, replyKeyboardFactory.mainMenu());
    }
}
