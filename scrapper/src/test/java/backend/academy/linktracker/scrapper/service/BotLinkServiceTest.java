package backend.academy.linktracker.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonLinkResponse;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.link.TrackedResourceResolver;
import backend.academy.linktracker.scrapper.link.handlers.LinkHandler;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Filter;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.FilterRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BotLinkServiceTest {

    private static final long CHAT_ID = 1L;
    private static final long LINK_ID = 10L;
    private static final long SUBSCRIPTION_ID = 100L;
    private static final URI TEST_URI = URI.create("https://github.com/octocat/Hello-World");
    private static final TrackedResource RESOURCE = TrackedResource.values()[0];

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private FilterRepository filterRepository;

    private BotLinkService botLinkService;

    private Chat chat;

    @BeforeEach
    void setUp() {
        chat = new Chat(CHAT_ID);
        LinkSubscriptionService linkSubscriptionService = getLinkSubscriptionService();
        ChatLookupService chatLookupService = new ChatLookupService(chatRepository);
        botLinkService = new BotLinkService(
                subscriptionRepository,
            chatLookupService,
            linkSubscriptionService,
                new SubscriptionMetadataService(tagRepository, filterRepository));
    }

    private @NonNull LinkSubscriptionService getLinkSubscriptionService() {
        TrackedResourceResolver trackedResourceResolver = new TrackedResourceResolver(List.of(new LinkHandler() {
            @Override
            public Set<String> supportedHosts() {
                return Set.of("github.com");
            }

            @Override
            public TrackedResource resource() {
                return RESOURCE;
            }
        }));
        return new LinkSubscriptionService(linkRepository, subscriptionRepository, trackedResourceResolver);
    }

    @Test
    void addLink_shouldCreateSubscriptionBindNormalizedTagsAndFilters_andReturnResponse()
            throws ChatNotFoundException, LinkAlreadyTrackedException {
        CommonAddLinkRequest request = new CommonAddLinkRequest(
                TEST_URI,
                Arrays.asList(" java ", "backend", "java", "", "   ", null),
                Arrays.asList(" status:open ", "status:open", "author:me", null, " "));

        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(chat));
        when(linkRepository.findByUriAndTrackedResource(TEST_URI, RESOURCE)).thenReturn(Optional.empty());

        doAnswer(invocation -> {
                    Link link = invocation.getArgument(0);
                    link.setId(LINK_ID);
                    return null;
                })
                .when(linkRepository)
                .addLink(any(Link.class));

        doAnswer(invocation -> {
                    Subscription subscription = invocation.getArgument(0);
                    subscription.setId(SUBSCRIPTION_ID);
                    return true;
                })
                .when(subscriptionRepository)
                .addSubscription(any(Subscription.class));

        Tag javaTag = mockTagWithId(11L, "java");
        Tag backendTag = mockTagWithId(12L, "backend");
        Filter statusOpen = mockFilterWithId(21L, "status:open");
        Filter authorMe = mockFilterWithId(22L, "author:me");

        when(tagRepository.findByChatIdAndName(CHAT_ID, "java")).thenReturn(Optional.of(javaTag));
        when(tagRepository.findByChatIdAndName(CHAT_ID, "backend")).thenReturn(Optional.of(backendTag));
        when(filterRepository.findByChatIdAndValue(CHAT_ID, "status:open")).thenReturn(Optional.of(statusOpen));
        when(filterRepository.findByChatIdAndValue(CHAT_ID, "author:me")).thenReturn(Optional.of(authorMe));

        when(tagRepository.findAllBySubscription(SUBSCRIPTION_ID)).thenReturn(List.of(javaTag, backendTag));
        when(filterRepository.findAllBySubscription(SUBSCRIPTION_ID)).thenReturn(List.of(statusOpen, authorMe));

        CommonLinkResponse response = botLinkService.addLink(CHAT_ID, request);

        assertEquals(LINK_ID, response.id());
        assertEquals(TEST_URI, response.url());
        assertIterableEquals(List.of("java", "backend"), response.tags());
        assertIterableEquals(List.of("status:open", "author:me"), response.filters());

        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).addSubscription(subscriptionCaptor.capture());

        Subscription savedSubscription = subscriptionCaptor.getValue();
        assertEquals(chat, savedSubscription.getChat());
        assertEquals(LINK_ID, savedSubscription.getLink().getId());
        assertEquals(TEST_URI, savedSubscription.getLink().getUri());

        verify(tagRepository).bindToSubscription(SUBSCRIPTION_ID, 11L);
        verify(tagRepository).bindToSubscription(SUBSCRIPTION_ID, 12L);
        verify(filterRepository).bindToSubscription(SUBSCRIPTION_ID, 21L);
        verify(filterRepository).bindToSubscription(SUBSCRIPTION_ID, 22L);
    }

    @Test
    void addLink_shouldThrowWhenSubscriptionAlreadyExists() throws ChatNotFoundException {
        Link existingLink = new Link(TEST_URI, RESOURCE);
        existingLink.setId(LINK_ID);

        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(chat));
        when(linkRepository.findByUriAndTrackedResource(TEST_URI, RESOURCE)).thenReturn(Optional.of(existingLink));
        when(subscriptionRepository.addSubscription(any(Subscription.class))).thenReturn(false);

        assertThrows(
                LinkAlreadyTrackedException.class,
                () -> botLinkService.addLink(
                        CHAT_ID, new CommonAddLinkRequest(TEST_URI, List.of("java"), List.of("f:1"))));

        verify(tagRepository, never()).bindToSubscription(anyLong(), anyLong());
        verify(filterRepository, never()).bindToSubscription(anyLong(), anyLong());
        verify(linkRepository, never()).addLink(any(Link.class));
    }

    @Test
    void deleteLink_shouldReturnDeletedLink_deleteSubscription_andDeleteOrphanLink() throws ChatNotFoundException {
        Link link = new Link(TEST_URI, RESOURCE);
        link.setId(LINK_ID);

        Subscription subscription = new Subscription(chat, link);
        subscription.setId(SUBSCRIPTION_ID);

        Tag javaTag = mockTagNameOnly("java");
        Filter statusOpen = mockFilterValueOnly("status:open");

        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(chat));
        when(linkRepository.findByUriAndTrackedResource(TEST_URI, RESOURCE)).thenReturn(Optional.of(link));
        when(subscriptionRepository.findByChatIdAndLinkId(CHAT_ID, LINK_ID)).thenReturn(Optional.of(subscription));
        when(tagRepository.findAllBySubscription(SUBSCRIPTION_ID)).thenReturn(List.of(javaTag));
        when(filterRepository.findAllBySubscription(SUBSCRIPTION_ID)).thenReturn(List.of(statusOpen));
        when(subscriptionRepository.getAllChatsByLink(link)).thenReturn(List.of());

        CommonLinkResponse response = botLinkService.deleteLink(CHAT_ID, new CommonRemoveLinkRequest(TEST_URI));

        assertEquals(LINK_ID, response.id());
        assertEquals(TEST_URI, response.url());
        assertIterableEquals(List.of("java"), response.tags());
        assertIterableEquals(List.of("status:open"), response.filters());

        verify(tagRepository).findAllBySubscription(SUBSCRIPTION_ID);
        verify(filterRepository).findAllBySubscription(SUBSCRIPTION_ID);
        verify(subscriptionRepository).deleteSubscription(subscription);
        verify(subscriptionRepository).getAllChatsByLink(link);
        verify(linkRepository).deleteById(LINK_ID);

        verify(tagRepository, never()).unbindFromSubscription(anyLong(), anyLong());
        verify(filterRepository, never()).unbindFromSubscription(anyLong(), anyLong());
    }

    @Test
    void deleteLink_shouldReturnEmptyResponseWhenSubscriptionDoesNotExist() throws ChatNotFoundException {
        Link link = new Link(TEST_URI, RESOURCE);
        link.setId(LINK_ID);

        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(chat));
        when(linkRepository.findByUriAndTrackedResource(TEST_URI, RESOURCE)).thenReturn(Optional.of(link));
        when(subscriptionRepository.findByChatIdAndLinkId(CHAT_ID, LINK_ID)).thenReturn(Optional.empty());

        CommonLinkResponse response = botLinkService.deleteLink(CHAT_ID, new CommonRemoveLinkRequest(TEST_URI));

        assertNull(response.id());
        assertEquals(TEST_URI, response.url());
        assertIterableEquals(List.of(), response.tags());
        assertIterableEquals(List.of(), response.filters());

        verify(subscriptionRepository, never()).deleteSubscription(any(Subscription.class));
        verify(linkRepository, never()).deleteById(anyLong());
        verify(tagRepository, never()).unbindFromSubscription(anyLong(), anyLong());
        verify(filterRepository, never()).unbindFromSubscription(anyLong(), anyLong());
    }

    @Test
    void getLinks_shouldReturnAllLinksWithTagsAndFilters() throws ChatNotFoundException {
        URI firstUri = URI.create("https://github.com/org/repo");
        URI secondUri = URI.create("https://github.com/org/another");

        Link firstLink = new Link(firstUri, RESOURCE);
        firstLink.setId(1L);

        Link secondLink = new Link(secondUri, RESOURCE);
        secondLink.setId(2L);

        Subscription firstSubscription = new Subscription(chat, firstLink);
        firstSubscription.setId(101L);

        Subscription secondSubscription = new Subscription(chat, secondLink);
        secondSubscription.setId(102L);

        Tag javaTag = mockTagNameOnly("java");
        Tag backendTag = mockTagNameOnly("backend");
        Filter firstFilter = mockFilterValueOnly("status:open");
        Filter secondFilter = mockFilterValueOnly("label:bug");

        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(chat));
        when(subscriptionRepository.getAllSubscriptionsByChat(chat))
                .thenReturn(List.of(firstSubscription, secondSubscription));

        when(tagRepository.findAllBySubscription(101L)).thenReturn(List.of(javaTag));
        when(filterRepository.findAllBySubscription(101L)).thenReturn(List.of(firstFilter));
        when(tagRepository.findAllBySubscription(102L)).thenReturn(List.of(backendTag));
        when(filterRepository.findAllBySubscription(102L)).thenReturn(List.of(secondFilter));

        CommonListLinksResponse response = botLinkService.getLinks(CHAT_ID);

        assertEquals(2, response.size());
        assertEquals(2, response.links().size());

        CommonLinkResponse first = response.links().getFirst();
        assertEquals(1L, first.id());
        assertEquals(firstUri, first.url());
        assertIterableEquals(List.of("java"), first.tags());
        assertIterableEquals(List.of("status:open"), first.filters());

        CommonLinkResponse second = response.links().get(1);
        assertEquals(2L, second.id());
        assertEquals(secondUri, second.url());
        assertIterableEquals(List.of("backend"), second.tags());
        assertIterableEquals(List.of("label:bug"), second.filters());
    }

    @Test
    void getLinks_shouldThrowWhenChatNotFound() {
        when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.empty());

        assertThrows(ChatNotFoundException.class, () -> botLinkService.getLinks(CHAT_ID));

        verify(subscriptionRepository, never()).getAllSubscriptionsByChat(any(Chat.class));
    }

    private Tag mockTagWithId(Long id, String name) {
        Tag tag = mock(Tag.class);
        when(tag.getId()).thenReturn(id);
        when(tag.getName()).thenReturn(name);
        return tag;
    }

    private Tag mockTagNameOnly(String name) {
        Tag tag = mock(Tag.class);
        when(tag.getName()).thenReturn(name);
        return tag;
    }

    private Filter mockFilterWithId(Long id, String value) {
        Filter filter = mock(Filter.class);
        when(filter.getId()).thenReturn(id);
        when(filter.getValue()).thenReturn(value);
        return filter;
    }

    private Filter mockFilterValueOnly(String value) {
        Filter filter = mock(Filter.class);
        when(filter.getValue()).thenReturn(value);
        return filter;
    }
}
