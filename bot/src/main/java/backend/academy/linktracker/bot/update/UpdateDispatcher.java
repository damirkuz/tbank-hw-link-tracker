package backend.academy.linktracker.bot.update;

import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.update.context.UpdateEnvelope;
import backend.academy.linktracker.bot.update.context.UpdateEnvelopeFactory;
import backend.academy.linktracker.bot.update.router.CallbackRouter;
import backend.academy.linktracker.bot.update.router.CommandRouter;
import backend.academy.linktracker.bot.update.router.GlobalActionRouter;
import backend.academy.linktracker.bot.update.router.StateRouter;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateDispatcher {
    private final TelegramBot bot;
    private final CommandRouter commandRouter;
    private final StateRouter stateRouter;
    private final UpdateEnvelopeFactory updateEnvelopeFactory;
    private final GlobalActionRouter globalActionRouter;
    private final CallbackRouter callbackRouter;

    public void dispatch(Update update) {
        dispatch(updateEnvelopeFactory.create(update));
    }

    public void dispatch(UpdateEnvelope envelope) {
        logReceivedUpdate(envelope);

        if (globalActionRouter.tryHandle(envelope.update(), envelope.context())) {
            logRoute(envelope, "Обновление обработано глобальным роутером");
            return;
        }

        if (envelope.context().hasCallbackData()) {
            logRoute(envelope, "Направляем обновление в роутер callback");
            callbackRouter.route(envelope.update(), envelope.context());
            return;
        }

        if (isCommand(envelope.context())) {
            logRoute(envelope, "Направляем обновление в роутер команд");
            commandRouter.route(envelope.update(), envelope.context());
            return;
        }

        logRoute(envelope, "Направляем обновление в роутер состояний");
        stateRouter.route(envelope.update(), envelope.context());
    }

    @PostConstruct
    public void init() {
        bot.setUpdatesListener(this::handleUpdates);
        log.info("Инициализирован listener входящих обновлений Telegram");
    }

    private int handleUpdates(List<Update> updates) {
        log.atDebug().addKeyValue("updates_count", updates.size()).log("Получен пакет обновлений");

        for (Update update : updates) {
            UpdateEnvelope envelope = updateEnvelopeFactory.create(update);

            try {
                dispatch(envelope);
            } catch (Exception e) {
                log.atError()
                        .setCause(e)
                        .addKeyValue("update_id", envelope.context().updateId())
                        .addKeyValue("chat_id", envelope.context().chatId())
                        .addKeyValue("user_id", envelope.context().userId())
                        .log("Ошибка во время обработки обновления");
            }
        }

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private boolean isCommand(UpdateContext context) {
        return context.hasMessageText() && context.messageText().startsWith("/");
    }

    private void logReceivedUpdate(UpdateEnvelope envelope) {
        UpdateContext context = envelope.context();

        log.atDebug()
                .addKeyValue("update_id", context.updateId())
                .addKeyValue("chat_id", context.chatId())
                .addKeyValue("user_id", context.userId())
                .addKeyValue("has_text", context.hasMessageText())
                .addKeyValue("has_callback", context.hasCallbackData())
                .log("Получено входящее обновление");
    }

    private void logRoute(UpdateEnvelope envelope, String message) {
        UpdateContext context = envelope.context();

        log.atDebug()
                .addKeyValue("update_id", context.updateId())
                .addKeyValue("chat_id", context.chatId())
                .addKeyValue("user_id", context.userId())
                .log(message);
    }
}
