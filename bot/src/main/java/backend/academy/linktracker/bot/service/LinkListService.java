package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.contracts.dto.response.CommonLinkResponse;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkListService {

    private final ScrapperGateway scrapperClient;
    private final BotTextService botTextService;

    public String buildListMessage(long chatId, String tag) {
        CommonListLinksResponse response = getLinksWithAutoRegister(chatId);

        List<String> lines = new ArrayList<>();
        for (CommonLinkResponse link : response.links()) {
            if (tag.isBlank() || link.tags().contains(tag)) {
                StringBuilder line =
                        new StringBuilder(botTextService.get("bot.list.link-in-list", link.id(), link.url()));
                if (link.tags() != null && !link.tags().isEmpty()) {
                    line.append(" ")
                            .append(botTextService.get("bot.list.link-in-list-tags", String.join(", ", link.tags())));
                }
                lines.add(line.toString());
            }
        }

        if (lines.isEmpty()) {
            return botTextService.get("bot.list.empty-link-list");
        }

        String header = tag.isBlank()
                ? botTextService.get("bot.list.link-list")
                : botTextService.get("bot.list.link-list-filtered", tag);

        return header + "\n" + String.join("\n", lines);
    }

    public Set<String> collectTags(long chatId) {
        return getLinksWithAutoRegister(chatId).links().stream()
                .filter(l -> l.tags() != null)
                .flatMap(l -> l.tags().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private CommonListLinksResponse getLinksWithAutoRegister(long chatId) {
        try {
            return scrapperClient.getLinks(chatId);
        } catch (ChatNotFoundException e) {
            scrapperClient.registerChat(chatId);
            return scrapperClient.getLinks(chatId);
        }
    }
}
