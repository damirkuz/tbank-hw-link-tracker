package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.service.BotChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BotChatController {

    private final BotChatService botChatService;

    @PostMapping("/tg-chat/{chatId}")
    public void addChat(@PathVariable long chatId) {
        botChatService.registerChat(chatId);
    }

    @DeleteMapping("/tg-chat/{chatId}")
    public void deleteChat(@PathVariable long chatId) {
        botChatService.deleteChat(chatId);
    }
}
