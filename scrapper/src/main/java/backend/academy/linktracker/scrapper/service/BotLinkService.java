package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonLinkResponse;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BotLinkService {

    private final SubscriptionRepository subscriptionRepository;
    private final ChatLookupService chatLookupService;
    private final LinkSubscriptionService linkSubscriptionService;
    private final SubscriptionMetadataService subscriptionMetadataService;

    @Transactional
    public CommonLinkResponse addLink(long chatId, CommonAddLinkRequest addLinkRequest)
            throws ChatNotFoundException, LinkAlreadyTrackedException {
        Chat chat = chatLookupService.getRequiredChat(chatId);
        Link link = linkSubscriptionService.getOrCreateLink(addLinkRequest.uri());

        Subscription subscription = new Subscription(chat, link);
        boolean added = subscriptionRepository.addSubscription(subscription);

        if (!added) {
            throw new LinkAlreadyTrackedException();
        }

        subscriptionMetadataService.bindMetadata(subscription, addLinkRequest.tags(), addLinkRequest.filters());

        return subscriptionMetadataService.toResponse(subscription);
    }

    @Transactional
    public CommonLinkResponse deleteLink(long chatId, CommonRemoveLinkRequest removeLinkRequest)
            throws ChatNotFoundException {
        Chat chat = chatLookupService.getRequiredChat(chatId);

        var subscriptionOptional = linkSubscriptionService.findSubscription(chat, removeLinkRequest.uri());
        if (subscriptionOptional.isEmpty()) {
            return new CommonLinkResponse(null, removeLinkRequest.uri(), List.of(), List.of());
        }

        Subscription subscription = subscriptionOptional.orElseThrow();
        CommonLinkResponse response = subscriptionMetadataService.toResponse(subscription);

        subscriptionRepository.deleteSubscription(subscription);
        linkSubscriptionService.deleteLinkIfOrphan(subscription.getLink());

        return response;
    }

    @Transactional(readOnly = true)
    public CommonListLinksResponse getLinks(long chatId) throws ChatNotFoundException {
        List<Subscription> subscriptionList = getSubscriptionsByChatId(chatId);
        List<CommonLinkResponse> linkResponses = new ArrayList<>(subscriptionList.size());

        for (Subscription subscription : subscriptionList) {
            linkResponses.add(subscriptionMetadataService.toResponse(subscription));
        }

        return new CommonListLinksResponse(linkResponses, linkResponses.size());
    }

    @Transactional(readOnly = true)
    public List<Subscription> getSubscriptionsByChatId(long chatId) throws ChatNotFoundException {
        Chat chat = chatLookupService.getRequiredChat(chatId);
        return subscriptionRepository.getAllSubscriptionsByChat(chat);
    }
}
