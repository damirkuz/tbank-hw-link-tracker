package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonLinkResponse;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Subscription;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.util.TrackedResourceResolver;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotLinkService {

    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final SubscriptionRepository subscriptionRepository;

    private final TrackedResourceResolver trackedResourceResolver;

    public CommonLinkResponse addLink(long chatId, CommonAddLinkRequest addLinkRequest) {
        Chat chat = chatRepository.getChat(chatId);

        Link link = createLink(addLinkRequest.uri());
        linkRepository.addLink(link);

        Subscription subscription = new Subscription(chat, link, addLinkRequest.tags(), addLinkRequest.filters());
        subscriptionRepository.addSubscription(subscription);

        return new CommonLinkResponse(link.getId(), link.getUri(), addLinkRequest.tags(), addLinkRequest.filters());
    }

    private Link createLink(URI uri) {
        // id высчитывается репозиторием
        TrackedResource trackedResource = trackedResourceResolver.resolve(uri);
        return new Link(uri, trackedResource);
    }

    public CommonLinkResponse deleteLink(long chatId, CommonRemoveLinkRequest removeLinkRequest) {
        Chat chat = chatRepository.getChat(chatId);
        Link link = createLink(removeLinkRequest.uri());

        Subscription subscription = new Subscription(chat, link, null, null);
        subscriptionRepository.deleteSubscription(subscription);

        // намеренно возвращаю null
        // зачем вообще возвращать всё, что содержала ссылка, специально собирать это, если мы её удаляем
        // когда перейдём на бд, поменяю на конкретные значения
        return new CommonLinkResponse(null, removeLinkRequest.uri(), null, null);
    }

    public CommonListLinksResponse getLinks(long chatId) {
        List<Subscription> subscriptionList = getSubscriptionsByChatId(chatId);
        List<CommonLinkResponse> linkResponses = new ArrayList<>();

        for (Subscription subscription : subscriptionList) {
            linkResponses.add(new CommonLinkResponse(
                    subscription.getLink().getId(),
                    subscription.getLink().getUri(),
                    subscription.getTags(),
                    subscription.getFilters()));
        }

        return new CommonListLinksResponse(linkResponses, linkResponses.size());
    }

    public List<Subscription> getSubscriptionsByChatId(long chatId) {
        Chat chat = chatRepository.getChat(chatId);
        return subscriptionRepository.getAllSubscriptionsByChat(chat);
    }
}
