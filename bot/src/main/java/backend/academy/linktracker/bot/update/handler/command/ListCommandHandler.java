package backend.academy.linktracker.bot.update.handler.command;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.service.BotOperations;
import backend.academy.linktracker.bot.service.LinkListService;
import backend.academy.linktracker.bot.service.keyboard.ReplyKeyboardFactory;
import backend.academy.linktracker.bot.service.keyboard.TagKeyboardFactory;
import backend.academy.linktracker.bot.update.context.UpdateContext;
import backend.academy.linktracker.bot.util.StringParser;
import com.pengrad.telegrambot.model.Update;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("/list")
@RequiredArgsConstructor
public class ListCommandHandler implements CommandHandler {

    private final BotOperations botOperations;
    private final LinkListService linkListService;
    private final TagKeyboardFactory tagKeyboardFactory;
    private final ReplyKeyboardFactory replyKeyboardFactory;
    private final BotProperties botProperties;

    @Override
    public void handle(Update update, UpdateContext updateContext) {
        long chatId = updateContext.chatId();
        String text = update.message().text();

        String command = botProperties.commandOf("list-command");
        String tag = text.startsWith(command)
                ? StringParser.parseAfterSpace(text).trim().toLowerCase(Locale.ROOT)
                : "";

        String answer = linkListService.buildListMessage(chatId, tag);
        Set<String> tags = linkListService.collectTags(chatId);

        if (tag.isBlank() && !tags.isEmpty()) {
            botOperations.sendMessage(chatId, answer, tagKeyboardFactory.tagFilter(tags));
        } else {
            botOperations.sendMessage(chatId, answer);
        }
    }
}
