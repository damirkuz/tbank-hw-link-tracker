// package backend.academy.linktracker.scrapper.scheduler;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
//
// import backend.academy.linktracker.contracts.dto.request.CommonLinkUpdate;
// import backend.academy.linktracker.scrapper.client.protocol.BotGateway;
// import backend.academy.linktracker.scrapper.client.provider.BaseTrackedClient;
// import backend.academy.linktracker.scrapper.config.properties.SchedulerProperties;
// import backend.academy.linktracker.scrapper.model.Chat;
// import backend.academy.linktracker.scrapper.model.Link;
// import backend.academy.linktracker.scrapper.model.TrackedResource;
// import backend.academy.linktracker.scrapper.repository.LinkRepository;
// import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
// import java.net.URI;
// import java.time.Instant;
// import java.util.List;
// import java.util.Map;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
//
// @ExtendWith(MockitoExtension.class)
// @DisplayName("UpdateChecker")
// class UpdateCheckerTest {
//
//    @Mock
//    private BaseTrackedClient githubClient;
//
//    @Mock
//    private LinkRepository linkRepository;
//
//    @Mock
//    private SubscriptionRepository subscriptionRepository;
//
//    @Mock
//    private BotGateway botClient;
//
//    @Mock
//    private SchedulerProperties schedulerProperties;
//
//    private UpdateChecker checker;
//
//    private static final URI GITHUB_URI = URI.create("https://github.com/user/repo");
//
//    @BeforeEach
//    void setUp() {
//        Map<TrackedResource, BaseTrackedClient> clients = Map.of(TrackedResource.GITHUB, githubClient);
//        checker = new UpdateChecker(clients, linkRepository, subscriptionRepository, botClient, schedulerProperties);
//    }
//
//    @Test
//    @DisplayName("getUpdates — при firstUpdate=null сохраняет lastUpdate, не отправляет уведомление")
//    void firstUpdateSavedNoNotification() {
//        Link link = new Link(GITHUB_URI, TrackedResource.GITHUB);
//        // lastUpdate == null
//
//        Instant now = Instant.now();
//        when(linkRepository.getLinks()).thenReturn(List.of(link));
//        when(githubClient.getLastUpdate(GITHUB_URI)).thenReturn(now);
//
//        checker.getUpdates();
//
//        assertThat(link.getLastUpdate()).isEqualTo(now);
//        verify(botClient, never()).sendUpdate(any());
//    }
//
//    @Test
//    @DisplayName("getUpdates — нет нового обновления, sendUpdate не вызывается")
//    void noNewUpdateNoNotification() {
//        Instant past = Instant.parse("2024-01-01T00:00:00Z");
//        Link link = new Link(GITHUB_URI, TrackedResource.GITHUB);
//        link.setLastUpdate(past);
//
//        when(linkRepository.getLinks()).thenReturn(List.of(link));
//        when(githubClient.getLastUpdate(GITHUB_URI)).thenReturn(past); // не после
//
//        checker.getUpdates();
//
//        verify(botClient, never()).sendUpdate(any());
//    }
//
//    @Test
//    @DisplayName("getUpdates — есть новое обновление, sendUpdate вызывается с правильными данными")
//    void newUpdateSendsNotification() {
//        Instant old = Instant.parse("2024-01-01T00:00:00Z");
//        Instant newer = Instant.parse("2024-06-01T00:00:00Z");
//
//        Link link = new Link(GITHUB_URI, TrackedResource.GITHUB);
//        link.setId(42L);
//        link.setLastUpdate(old);
//
//        Chat chat1 = new Chat(100L);
//        Chat chat2 = new Chat(200L);
//
//        when(linkRepository.getLinks()).thenReturn(List.of(link));
//        when(githubClient.getLastUpdate(GITHUB_URI)).thenReturn(newer);
//        when(subscriptionRepository.getAllChatsByLink(link)).thenReturn(List.of(chat1, chat2));
//
//        checker.getUpdates();
//
//        ArgumentCaptor<CommonLinkUpdate> captor = ArgumentCaptor.forClass(CommonLinkUpdate.class);
//        verify(botClient).sendUpdate(captor.capture());
//
//        CommonLinkUpdate sent = captor.getValue();
//        assertThat(sent.id()).isEqualTo(42L);
//        assertThat(sent.url()).isEqualTo(GITHUB_URI);
//        assertThat(sent.tgChatIds()).containsExactlyInAnyOrder(100L, 200L);
//        assertThat(sent.description()).isNotBlank();
//    }
//
//    @Test
//    @DisplayName("getUpdates — lastUpdate обновляется после уведомления")
//    void lastUpdateIsUpdatedAfterNotification() {
//        Instant old = Instant.parse("2024-01-01T00:00:00Z");
//        Instant newer = Instant.parse("2024-06-01T00:00:00Z");
//
//        Link link = new Link(GITHUB_URI, TrackedResource.GITHUB);
//        link.setId(1L);
//        link.setLastUpdate(old);
//
//        when(linkRepository.getLinks()).thenReturn(List.of(link));
//        when(githubClient.getLastUpdate(GITHUB_URI)).thenReturn(newer);
//        when(subscriptionRepository.getAllChatsByLink(link)).thenReturn(List.of(new Chat(1L)));
//
//        checker.getUpdates();
//
//        assertThat(link.getLastUpdate()).isEqualTo(newer);
//    }
//
//    @Test
//    @DisplayName("getUpdates — нет ссылок, ничего не происходит")
//    void emptyLinksNoInteraction() {
//        when(linkRepository.getLinks()).thenReturn(List.of());
//
//        checker.getUpdates();
//
//        verify(botClient, never()).sendUpdate(any());
//        verify(githubClient, never()).getLastUpdate(any());
//    }
//
//    @Test
//    @DisplayName("getUpdates — несколько ссылок, уведомление только по обновлённым")
//    void multipleLinksOnlyUpdatedAreNotified() {
//        Instant old = Instant.parse("2024-01-01T00:00:00Z");
//        Instant newer = Instant.parse("2024-06-01T00:00:00Z");
//
//        URI uri1 = URI.create("https://github.com/user/repo1");
//        URI uri2 = URI.create("https://github.com/user/repo2");
//
//        Link updatedLink = new Link(uri1, TrackedResource.GITHUB);
//        updatedLink.setId(1L);
//        updatedLink.setLastUpdate(old);
//
//        Link notUpdatedLink = new Link(uri2, TrackedResource.GITHUB);
//        notUpdatedLink.setId(2L);
//        notUpdatedLink.setLastUpdate(old);
//
//        when(linkRepository.getLinks()).thenReturn(List.of(updatedLink, notUpdatedLink));
//        when(githubClient.getLastUpdate(uri1)).thenReturn(newer);
//        when(githubClient.getLastUpdate(uri2)).thenReturn(old); // без изменений
//        when(subscriptionRepository.getAllChatsByLink(updatedLink)).thenReturn(List.of(new Chat(1L)));
//
//        checker.getUpdates();
//
//        verify(botClient).sendUpdate(any()); // ровно один раз
//    }
//
//    @Test
//    @DisplayName("getLastUpdate — делегирует в правильный клиент по TrackedResource")
//    void getLastUpdateDelegatesToClient() {
//        Instant expected = Instant.now();
//        when(githubClient.getLastUpdate(GITHUB_URI)).thenReturn(expected);
//
//        Instant result = checker.getLastUpdate(GITHUB_URI, TrackedResource.GITHUB);
//
//        assertThat(result).isEqualTo(expected);
//    }
// }
