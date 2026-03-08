package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotChatService {

    private final ChatRepository chatRepository;

    public void registerChat(long chatId) {
        chatRepository.registerChat(new Chat(chatId));
    }

    public void deleteChat(long chatId) {
        chatRepository.deleteChat(new Chat(chatId));
    }
}
