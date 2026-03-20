package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.generated.api.UpdatesApi;
import backend.academy.linktracker.bot.generated.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.LinkUpdateService;
import backend.academy.linktracker.contracts.dto.mapper.BotHttpMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ScrapperController implements UpdatesApi {

    private final LinkUpdateService linkUpdateService;

    @Override
    public ResponseEntity<Void> updatesPost(LinkUpdate linkUpdate) {
        logInfo(linkUpdate).log("Получен HTTP запрос на отправку обновления");

        try {
            linkUpdateService.handleLinkUpdate(BotHttpMapper.fromLinkUpdate(linkUpdate));
            logInfo(linkUpdate).log("HTTP запрос успешно обработан");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logFailure(linkUpdate, e);
            throw e;
        }
    }

    private void logFailure(LinkUpdate linkUpdate, Exception e) {
        addCommonFields(log.atError(), linkUpdate)
                .setCause(e)
                .addKeyValue("exception", e.getClass().getSimpleName())
                .log("Ошибка при обработке HTTP запроса");
    }

    private LoggingEventBuilder logInfo(LinkUpdate linkUpdate) {
        return addCommonFields(log.atInfo(), linkUpdate);
    }

    private LoggingEventBuilder addCommonFields(LoggingEventBuilder builder, LinkUpdate linkUpdate) {
        return builder.addKeyValue("transport", "http")
                .addKeyValue("operation", "updatesPost")
                .addKeyValue("link_id", linkUpdate.getId())
                .addKeyValue("url", linkUpdate.getUrl())
                .addKeyValue(
                        "tg_chat_ids_count",
                        linkUpdate.getTgChatIds() == null
                                ? 0
                                : linkUpdate.getTgChatIds().size());
    }
}
