package backend.academy.linktracker.scrapper.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BotChatService")
class BotChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private BotChatService service;

    // --- registerChat ---

    @Test
    @DisplayName("registerChat — успешно регистрирует новый чат")
    void registerChatSuccess() {
        when(chatRepository.registerChat(any(Chat.class))).thenReturn(true);

        service.registerChat(100L);

        verify(chatRepository).registerChat(new Chat(100L));
    }

    @Test
    @DisplayName("registerChat — бросает ChatAlreadyExistsException если чат уже есть")
    void registerChatAlreadyExists() {
        when(chatRepository.registerChat(any(Chat.class))).thenReturn(false);

        assertThatThrownBy(() -> service.registerChat(100L)).isInstanceOf(ChatAlreadyExistsException.class);
    }

    // --- deleteChat ---

    @Test
    @DisplayName("deleteChat — успешно удаляет чат")
    void deleteChatSuccess() {
        when(chatRepository.deleteChat(any(Chat.class))).thenReturn(true);

        service.deleteChat(100L);

        verify(chatRepository).deleteChat(new Chat(100L));
    }

    @Test
    @DisplayName("deleteChat — бросает ChatNotFoundException если чат не найден")
    void deleteChatNotFound() {
        when(chatRepository.deleteChat(any(Chat.class))).thenReturn(false);

        assertThatThrownBy(() -> service.deleteChat(100L)).isInstanceOf(ChatNotFoundException.class);
    }
}
