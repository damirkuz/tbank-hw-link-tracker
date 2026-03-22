package backend.academy.linktracker.bot.update.handler.command;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.util.StringParser;
import backend.academy.linktracker.contracts.dto.response.CommonLinkResponse;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import com.pengrad.telegrambot.model.Update;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("/list")
@RequiredArgsConstructor
public class ListCommandHandler implements CommandHandler {

    private final ScrapperGateway scrapperClient;
    private final BotTextService botTextService;
    private final BotOperations botOperations;

    @Override
    public void handle(Update update, UpdateContext updateContext) {
        long chatId = updateContext.chatId();

        String answer = "";
        CommonListLinksResponse commonListLinksResponse;
        try {
            commonListLinksResponse = scrapperClient.getLinks(chatId);
        } catch (ChatNotFoundException e) {
            scrapperClient.registerChat(chatId);
            commonListLinksResponse = scrapperClient.getLinks(chatId);
        }

        String tag =
                StringParser.parseAfterSpace(update.message().text()).trim().toLowerCase(Locale.ROOT);
        int count = 0;

        if (commonListLinksResponse.size() > 0) {
            StringBuilder sb = new StringBuilder(botTextService.get("bot.list.link-list"));
            for (CommonLinkResponse link : commonListLinksResponse.links()) {
                if (tag.isBlank() || link.tags().contains(tag)) {
                    count++;
                    sb.append("\n").append(botTextService.get("bot.list.link-in-list", link.id(), link.url()));
                    if (link.tags() != null) {
                        sb.append(" ")
                                .append(botTextService.get(
                                        "bot.list.link-in-list-tags", String.join(", ", link.tags())));
                    }
                }
            }
            answer = sb.toString();
        }

        if (count == 0) {
            answer = botTextService.get("bot.list.empty-link-list");
        }

        botOperations.sendMessage(chatId, answer);
    }
}
