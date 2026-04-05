package backend.academy.linktracker.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.contracts.dto.response.CommonLinkResponse;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("LinkListService")
class LinkListServiceTest {

    @Mock
    private ScrapperGateway scrapperClient;

    @Mock
    private BotTextService botTextService;

    @InjectMocks
    private LinkListService service;

    private static final URI LINK_1 = URI.create("https://github.com/user/repo");
    private static final URI LINK_2 = URI.create("https://example.com");

    @Test
    @DisplayName("buildListMessage — возвращает все ссылки если тег пустой")
    void shouldReturnAllLinksWhenTagBlank() {
        long chatId = 1L;
        CommonLinkResponse link = new CommonLinkResponse(1L, LINK_1, List.of("java"), null);
        when(scrapperClient.getLinks(chatId)).thenReturn(new CommonListLinksResponse(List.of(link), 1));
        when(botTextService.get("bot.list.link-list")).thenReturn("Ссылки:");
        when(botTextService.get("bot.list.link-in-list", 1L, LINK_1)).thenReturn("• " + LINK_1);
        when(botTextService.get("bot.list.link-in-list-tags", "java")).thenReturn("🏷 java");

        String result = service.buildListMessage(chatId, "");

        assertThat(result).isEqualTo("Ссылки:\n• " + LINK_1 + " 🏷 java");
    }

    @Test
    @DisplayName("buildListMessage — фильтрует по тегу")
    void shouldFilterByTag() {
        long chatId = 1L;
        CommonLinkResponse javaLink = new CommonLinkResponse(1L, LINK_1, List.of("java"), null);
        CommonLinkResponse otherLink = new CommonLinkResponse(2L, LINK_2, List.of("other"), null);
        when(scrapperClient.getLinks(chatId)).thenReturn(new CommonListLinksResponse(List.of(javaLink, otherLink), 2));
        when(botTextService.get("bot.list.link-list-filtered", "java")).thenReturn("Ссылки [java]:");
        when(botTextService.get("bot.list.link-in-list", 1L, LINK_1)).thenReturn("• " + LINK_1);
        when(botTextService.get("bot.list.link-in-list-tags", "java")).thenReturn("🏷 java");

        String result = service.buildListMessage(chatId, "java");

        assertThat(result).contains(LINK_1.toString());
        assertThat(result).doesNotContain(LINK_2.toString());
    }

    @Test
    @DisplayName("buildListMessage — возвращает empty-list если ничего не найдено")
    void shouldReturnEmptyMessageWhenNoLinks() {
        long chatId = 1L;
        when(scrapperClient.getLinks(chatId)).thenReturn(new CommonListLinksResponse(List.of(), 0));
        when(botTextService.get("bot.list.empty-link-list")).thenReturn("Список пуст.");

        String result = service.buildListMessage(chatId, "");

        assertThat(result).isEqualTo("Список пуст.");
    }

    @Test
    @DisplayName("buildListMessage — авторегистрирует чат при ChatNotFoundException")
    void shouldAutoRegisterChatOnNotFound() {
        long chatId = 1L;
        when(scrapperClient.getLinks(chatId))
                .thenThrow(new ChatNotFoundException("not found"))
                .thenReturn(new CommonListLinksResponse(List.of(), 0));
        when(botTextService.get("bot.list.empty-link-list")).thenReturn("Список пуст.");

        service.buildListMessage(chatId, "");

        verify(scrapperClient).registerChat(chatId);
    }

    @Test
    @DisplayName("collectTags — собирает уникальные теги из всех ссылок")
    void shouldCollectUniqueTags() {
        long chatId = 1L;
        CommonLinkResponse link1 = new CommonLinkResponse(1L, LINK_1, List.of("java", "spring"), null);
        CommonLinkResponse link2 = new CommonLinkResponse(2L, LINK_2, List.of("java", "other"), null);
        when(scrapperClient.getLinks(chatId)).thenReturn(new CommonListLinksResponse(List.of(link1, link2), 2));

        Set<String> tags = service.collectTags(chatId);

        assertThat(tags).containsExactlyInAnyOrder("java", "spring", "other");
    }
}
