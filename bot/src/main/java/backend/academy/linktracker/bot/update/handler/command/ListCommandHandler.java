package backend.academy.linktracker.bot.update.handler.command;

import backend.academy.linktracker.bot.client.ScrapperGateway;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.BotTextService;
import backend.academy.linktracker.bot.util.StringParser;
import backend.academy.linktracker.contracts.dto.response.LinkResponse;
import backend.academy.linktracker.contracts.dto.response.ListLinksResponse;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import com.pengrad.telegrambot.model.Update;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("/list")
@RequiredArgsConstructor
public class ListCommandHandler implements CommandHandler {

    private final ScrapperGateway scrapperClient;
    private final BotTextService botTextService;
    private final BotOperations botOperations;
    private final StringParser stringParser;

    @Override
    public boolean isCancelStateCommand() {
        return true;
    }

    @Override
    public void handle(Update update) {
        long chatId = update.message().chat().id();

        String answer = "";
        ListLinksResponse listLinksResponse;
        try {
            listLinksResponse = scrapperClient.getLinks(chatId);
        } catch (ChatNotFoundException e) {
            scrapperClient.registerChat(chatId);
            listLinksResponse = scrapperClient.getLinks(chatId);
        }

        String tag = stringParser.parseAfterSpace(update.message().text());
        int count = 0;

        if (listLinksResponse.size() > 0) {
            StringBuilder sb = new StringBuilder(botTextService.get("bot.list.link-list"));
            for (LinkResponse link : listLinksResponse.links()) {
                if (tag.isBlank() || Arrays.asList(link.tags()).contains(tag)) {
                    count++;
                    sb.append("\n")
                            .append(link.id())
                            .append(" ")
                            .append(link.url())
                            .append(" ");
                    if (link.tags() != null) {
                        sb.append(String.join(", ", link.tags()));
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
