package backend.academy.linktracker.scrapper.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotChatService {

    private final BotChatService botChatService;

    public void registerChat(long chatId) {
        botChatService.registerChat(chatId);
    }

    public void deleteChat(long chatId) {
        botChatService.deleteChat(chatId);
    }
}
