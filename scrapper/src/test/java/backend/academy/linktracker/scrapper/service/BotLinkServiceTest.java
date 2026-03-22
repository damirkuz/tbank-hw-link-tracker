package backend.academy.linktracker.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonLinkResponse;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.link.TrackedResourceResolver;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BotLinkService")
class BotLinkServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private TrackedResourceResolver trackedResourceResolver;

    @InjectMocks
    private BotLinkService service;

    private static final URI URI_LINK = URI.create("https://github.com/user/repo");
    private static final long CHAT_ID = 1L;
    private static final Chat CHAT = new Chat(CHAT_ID);

    // --- addLink ---

    @Test
    @DisplayName("addLink — успешно добавляет ссылку и возвращает ответ")
    void addLinkSuccess() {
        CommonAddLinkRequest request = new CommonAddLinkRequest(URI_LINK, List.of("java"), List.of());
        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(CHAT));
        when(trackedResourceResolver.resolve(URI_LINK)).thenReturn(TrackedResource.GITHUB);
        when(subscriptionRepository.addSubscription(any(Subscription.class))).thenReturn(true);

        CommonLinkResponse result = service.addLink(CHAT_ID, request);

        assertThat(result.url()).isEqualTo(URI_LINK);
        assertThat(result.tags()).containsExactly("java");
        verify(linkRepository).addLink(any(Link.class));
    }

    @Test
    @DisplayName("addLink — бросает ChatNotFoundException если чат не найден")
    void addLinkChatNotFound() {
        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addLink(CHAT_ID, new CommonAddLinkRequest(URI_LINK, List.of(), List.of())))
                .isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    @DisplayName("addLink — бросает LinkAlreadyTrackedException если подписка уже есть")
    void addLinkAlreadyTracked() {
        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(CHAT));
        when(trackedResourceResolver.resolve(URI_LINK)).thenReturn(TrackedResource.GITHUB);
        when(subscriptionRepository.addSubscription(any(Subscription.class))).thenReturn(false);

        assertThatThrownBy(() -> service.addLink(CHAT_ID, new CommonAddLinkRequest(URI_LINK, List.of(), List.of())))
                .isInstanceOf(LinkAlreadyTrackedException.class);
    }

    // --- deleteLink ---

    @Test
    @DisplayName("deleteLink — возвращает ответ с uri и null полями")
    void deleteLinkSuccess() {
        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(CHAT));
        when(trackedResourceResolver.resolve(URI_LINK)).thenReturn(TrackedResource.GITHUB);

        CommonLinkResponse result = service.deleteLink(CHAT_ID, new CommonRemoveLinkRequest(URI_LINK));

        assertThat(result.url()).isEqualTo(URI_LINK);
        assertThat(result.id()).isNull();
        assertThat(result.tags()).isNull();
        verify(subscriptionRepository).deleteSubscription(any(Subscription.class));
    }

    @Test
    @DisplayName("deleteLink — бросает ChatNotFoundException если чат не найден")
    void deleteLinkChatNotFound() {
        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteLink(CHAT_ID, new CommonRemoveLinkRequest(URI_LINK)))
                .isInstanceOf(ChatNotFoundException.class);
    }

    // --- getLinks ---

    @Test
    @DisplayName("getLinks — возвращает список подписок чата")
    void getLinksSuccess() {
        Link link = new Link(URI_LINK, TrackedResource.GITHUB);
        Subscription sub = new Subscription(CHAT, link, List.of("java"), List.of());
        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(CHAT));
        when(subscriptionRepository.getAllSubscriptionsByChat(CHAT)).thenReturn(List.of(sub));

        CommonListLinksResponse result = service.getLinks(CHAT_ID);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.links().get(0).url()).isEqualTo(URI_LINK);
        assertThat(result.links().get(0).tags()).containsExactly("java");
    }

    @Test
    @DisplayName("getLinks — возвращает пустой список если нет подписок")
    void getLinksEmpty() {
        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(CHAT));
        when(subscriptionRepository.getAllSubscriptionsByChat(CHAT)).thenReturn(List.of());

        CommonListLinksResponse result = service.getLinks(CHAT_ID);

        assertThat(result.size()).isZero();
        assertThat(result.links()).isEmpty();
    }

    @Test
    @DisplayName("getLinks — бросает ChatNotFoundException если чат не найден")
    void getLinksChatNotFound() {
        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLinks(CHAT_ID)).isInstanceOf(ChatNotFoundException.class);
    }

    // --- getChat (косвенно через getSubscriptionsByChatId) ---

    @Test
    @DisplayName("getSubscriptionsByChatId — возвращает подписки для существующего чата")
    void getSubscriptionsByChatId() {
        Link link = new Link(URI_LINK, TrackedResource.GITHUB);
        Subscription sub = new Subscription(CHAT, link, List.of(), List.of());
        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(CHAT));
        when(subscriptionRepository.getAllSubscriptionsByChat(CHAT)).thenReturn(List.of(sub));

        List<Subscription> result = service.getSubscriptionsByChatId(CHAT_ID);

        assertThat(result).hasSize(1);
    }
}
