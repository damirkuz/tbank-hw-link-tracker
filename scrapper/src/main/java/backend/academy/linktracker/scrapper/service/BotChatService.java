package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.repository.UserLinksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotChatService {

    private final UserLinksRepository userLinksRepository;

    public void registerChat(long chatId) {
        userLinksRepository.registerChat(chatId);
    }

    public void deleteChat(long chatId) {
        userLinksRepository.deleteChat(chatId);
    }
}
