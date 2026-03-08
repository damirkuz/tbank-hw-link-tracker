package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.common.request.AddLinkRequest;
import backend.academy.linktracker.common.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.UserLinksRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotLinkService {

    private final UserLinksRepository userLinksRepository;

    public void addLink(long chatId, AddLinkRequest addLinkRequest) {
//        userLinksService.addLink(chatId, );
    }

    public void deleteLink(long chatId, RemoveLinkRequest removeLinkRequest) {
//        userLinksService.deleteChat(chatId)
    }

    public List<Link> getLinks(long chatId) {
        return userLinksRepository.getLinks(chatId);
    }
}
