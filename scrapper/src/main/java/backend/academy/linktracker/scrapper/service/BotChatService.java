package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotChatService {

    private final ChatRepository chatRepository;

    public void registerChat(long chatId) throws ChatAlreadyExistsException {
        boolean added = chatRepository.registerChat(new Chat(chatId));
        if (!added) {
            throw new ChatAlreadyExistsException();
        }
    }

    public void deleteChat(long chatId) throws ChatNotFoundException {
        boolean deleted = chatRepository.deleteChat(new Chat(chatId));

        if (!deleted) {
            throw new ChatNotFoundException();
        }
    }
}
