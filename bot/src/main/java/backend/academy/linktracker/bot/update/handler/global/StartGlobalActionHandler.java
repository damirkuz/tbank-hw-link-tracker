package backend.academy.linktracker.bot.update.handler.global;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.service.StateStorage;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartGlobalActionHandler implements GlobalActionHandler {

    private final BotOperations botOperations;
    private final BotTextService botTextService;

    private final ScrapperGateway scrapperClient;

    private final StateStorage stateStorage;

    @Override
    public boolean supports(UpdateContext context) {
        return context.isCallbackOrCommandAction("start");
    }

    @Override
    public void handle(Update update, UpdateContext context) {
        if (context.chatId() == null) {
            return;
        }

        long chatId = update.message().chat().id();

        stateStorage.clearState(context.userId());
        botOperations.sendMessage(context.chatId(), botTextService.get("bot.common.start"));
        try {
            scrapperClient.registerChat(chatId);
        } catch (ChatAlreadyExistsException e) {
            log.atDebug().setCause(e).addKeyValue("chat_id", chatId).log("Чат уже был зарегистрирован в scrapper");
        }
    }
}
