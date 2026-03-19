package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.generated.api.TgChatApi;
import backend.academy.linktracker.scrapper.service.BotChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BotChatController implements TgChatApi {

    private final BotChatService botChatService;

    @Override
    public ResponseEntity<@NotNull Void> tgChatIdDelete(Long id) {
        log.atInfo().addKeyValue("chat_id", id).log("Запрос на удаление чата");

        botChatService.deleteChat(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<@NotNull Void> tgChatIdPost(Long id) {
        log.atInfo().addKeyValue("chat_id", id).log("Запрос на регистрацию чата");

        botChatService.registerChat(id);
        return ResponseEntity.ok().build();
    }
}
