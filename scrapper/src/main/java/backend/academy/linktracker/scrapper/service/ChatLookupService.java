package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatLookupService {

    private final ChatRepository chatRepository;

    @Transactional(readOnly = true)
    public Chat getRequiredChat(long chatId) throws ChatNotFoundException {
        return chatRepository.findById(chatId).orElseThrow(ChatNotFoundException::new);
    }
}
