package backend.academy.linktracker.scrapper.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.contracts.dto.request.CommonLinkUpdate;
import backend.academy.linktracker.scrapper.client.protocol.BotGateway;
import backend.academy.linktracker.scrapper.client.provider.BaseTrackedClient;
import backend.academy.linktracker.scrapper.config.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateChecker")
class UpdateCheckerTest {

    @Mock
    private BaseTrackedClient githubClient;

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private BotGateway botClient;

    private UpdateChecker checker;
    private Clock clock;
    private SchedulerProperties schedulerProperties;

    private static final URI GITHUB_URI = URI.create("https://github.com/user/repo");
    private static final int BATCH_SIZE = 100;
    private static final int INTERVAL_MS = 60_000;
    private static final Instant NOW = Instant.parse("2024-06-10T12:00:00Z");

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW, ZoneOffset.UTC);
        schedulerProperties = new SchedulerProperties(INTERVAL_MS, BATCH_SIZE);
        checker = new UpdateChecker(
                Map.of(TrackedResource.GITHUB, githubClient),
                linkRepository,
                subscriptionRepository,
                botClient,
                schedulerProperties,
                clock);
    }

    @Test
    @DisplayName("getUpdates — если ссылок нет, ничего не делает")
    void getUpdatesWhenNoLinksDoNothing() {
        when(linkRepository.findLinksForUpdate(any(OffsetDateTime.class), eq(0L), eq(BATCH_SIZE)))
                .thenReturn(List.of());

        checker.getUpdates();

        verify(linkRepository).findLinksForUpdate(any(OffsetDateTime.class), eq(0L), eq(BATCH_SIZE));
        verify(linkRepository, never()).updateCheckState(anyLong(), any(), any());
        verify(githubClient, never()).getLastUpdate(any());
        verify(botClient, never()).sendUpdate(any());
    }

    @Test
    @DisplayName("getUpdates — при первом обновлении сохраняет lastUpdate и не шлёт уведомление")
    void getUpdatesFirstObservationOnlyPersistState() {
        Instant actualLastUpdate = Instant.parse("2024-06-01T00:00:00Z");

        Link link = new Link(GITHUB_URI, TrackedResource.GITHUB);
        link.setId(1L);

        when(linkRepository.findLinksForUpdate(any(OffsetDateTime.class), eq(0L), eq(BATCH_SIZE)))
                .thenReturn(List.of(link))
                .thenReturn(List.of());
        when(githubClient.getLastUpdate(GITHUB_URI)).thenReturn(actualLastUpdate);

        checker.getUpdates();

        ArgumentCaptor<OffsetDateTime> nextCheckCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(linkRepository).updateCheckState(eq(1L), eq(actualLastUpdate), nextCheckCaptor.capture());
        verify(subscriptionRepository, never()).getAllChatsByLink(any());
        verify(botClient, never()).sendUpdate(any());

        OffsetDateTime nextCheckAt = nextCheckCaptor.getValue();
        assertThat(nextCheckAt).isEqualTo(OffsetDateTime.now(clock).plus(Duration.ofMillis(INTERVAL_MS)));
    }

    @Test
    @DisplayName("getUpdates — если нового обновления нет, уведомление не отправляет")
    void getUpdatesWhenNoFreshUpdateDoNotNotify() {
        Instant previousLastUpdate = Instant.parse("2024-06-01T00:00:00Z");

        Link link = new Link(GITHUB_URI, TrackedResource.GITHUB);
        link.setId(2L);
        link.setLastUpdate(previousLastUpdate);

        when(linkRepository.findLinksForUpdate(any(OffsetDateTime.class), eq(0L), eq(BATCH_SIZE)))
                .thenReturn(List.of(link))
                .thenReturn(List.of());
        when(githubClient.getLastUpdate(GITHUB_URI)).thenReturn(previousLastUpdate);

        checker.getUpdates();

        verify(linkRepository).updateCheckState(eq(2L), eq(previousLastUpdate), any(OffsetDateTime.class));
        verify(subscriptionRepository, never()).getAllChatsByLink(any());
        verify(botClient, never()).sendUpdate(any());
    }

    @Test
    @DisplayName("getUpdates — если есть новое обновление, обновляет state и шлёт уведомление")
    void getUpdatesWhenFreshUpdateSendNotification() {
        Instant previousLastUpdate = Instant.parse("2024-01-01T00:00:00Z");
        Instant actualLastUpdate = Instant.parse("2024-06-01T00:00:00Z");

        Link link = new Link(GITHUB_URI, TrackedResource.GITHUB);
        link.setId(42L);
        link.setLastUpdate(previousLastUpdate);

        Chat chat1 = new Chat(100L);
        Chat chat2 = new Chat(200L);

        when(linkRepository.findLinksForUpdate(any(OffsetDateTime.class), eq(0L), eq(BATCH_SIZE)))
                .thenReturn(List.of(link))
                .thenReturn(List.of());
        when(githubClient.getLastUpdate(GITHUB_URI)).thenReturn(actualLastUpdate);
        when(subscriptionRepository.getAllChatsByLink(link)).thenReturn(List.of(chat1, chat2));

        checker.getUpdates();

        verify(linkRepository).updateCheckState(eq(42L), eq(actualLastUpdate), any(OffsetDateTime.class));

        ArgumentCaptor<CommonLinkUpdate> captor = ArgumentCaptor.forClass(CommonLinkUpdate.class);
        verify(botClient).sendUpdate(captor.capture());

        CommonLinkUpdate sentUpdate = captor.getValue();
        assertThat(sentUpdate.id()).isEqualTo(42L);
        assertThat(sentUpdate.url()).isEqualTo(GITHUB_URI);
        assertThat(sentUpdate.description()).isEqualTo("Произошло обновление");
        assertThat(sentUpdate.tgChatIds()).containsExactly(100L, 200L);
    }

    @Test
    @DisplayName("getUpdates — обрабатывает ссылки батчами, пока репозиторий не вернёт пустой список")
    void getUpdatesProcessAllBatches() {
        schedulerProperties = new SchedulerProperties(INTERVAL_MS, 1);

        checker = new UpdateChecker(
                Map.of(TrackedResource.GITHUB, githubClient),
                linkRepository,
                subscriptionRepository,
                botClient,
                schedulerProperties,
                clock);

        Link first = new Link(URI.create("https://github.com/user/repo1"), TrackedResource.GITHUB);
        first.setId(1L);

        Link second = new Link(URI.create("https://github.com/user/repo2"), TrackedResource.GITHUB);
        second.setId(2L);

        when(linkRepository.findLinksForUpdate(any(OffsetDateTime.class), eq(0L), eq(1)))
                .thenReturn(List.of(first));
        when(linkRepository.findLinksForUpdate(any(OffsetDateTime.class), eq(1L), eq(1)))
                .thenReturn(List.of(second));
        when(linkRepository.findLinksForUpdate(any(OffsetDateTime.class), eq(2L), eq(1)))
                .thenReturn(List.of());

        when(githubClient.getLastUpdate(first.getUri())).thenReturn(Instant.parse("2024-06-01T00:00:00Z"));
        when(githubClient.getLastUpdate(second.getUri())).thenReturn(Instant.parse("2024-06-02T00:00:00Z"));

        checker.getUpdates();

        verify(linkRepository).findLinksForUpdate(any(OffsetDateTime.class), eq(0L), eq(1));
        verify(linkRepository).findLinksForUpdate(any(OffsetDateTime.class), eq(1L), eq(1));
        verify(linkRepository).findLinksForUpdate(any(OffsetDateTime.class), eq(2L), eq(1));
        verify(linkRepository, times(2)).updateCheckState(anyLong(), any(), any(OffsetDateTime.class));
    }

    @Test
    @DisplayName("getLastUpdate — делегирует вызов клиенту нужного ресурса")
    void getLastUpdateDelegatesToProperClient() {
        Instant expected = Instant.parse("2024-06-01T00:00:00Z");
        when(githubClient.getLastUpdate(GITHUB_URI)).thenReturn(expected);

        Instant result = checker.getLastUpdate(GITHUB_URI, TrackedResource.GITHUB);

        assertThat(result).isEqualTo(expected);
        verify(githubClient).getLastUpdate(GITHUB_URI);
    }
}
