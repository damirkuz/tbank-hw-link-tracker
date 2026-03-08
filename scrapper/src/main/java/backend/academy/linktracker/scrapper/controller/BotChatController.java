package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.service.BotChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BotChatController {

    private final BotChatService botChatService;

    @PostMapping("/tg-chat/{chatId}")
    public void addChat(@PathVariable long chatId) {
        log.atInfo().addKeyValue("chat_id", chatId).log("Запрос на регистрацию чата");

        botChatService.registerChat(chatId);
    }

    @DeleteMapping("/tg-chat/{chatId}")
    public void deleteChat(@PathVariable long chatId) {
        log.atInfo().addKeyValue("chat_id", chatId).log("Запрос на удаление чата");

        botChatService.deleteChat(chatId);
    }
}
