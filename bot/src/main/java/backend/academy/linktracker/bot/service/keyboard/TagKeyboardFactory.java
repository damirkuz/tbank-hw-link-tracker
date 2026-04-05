package backend.academy.linktracker.bot.service.keyboard;

import backend.academy.linktracker.bot.config.properties.BotProperties;
import backend.academy.linktracker.bot.service.BotTextService;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TagKeyboardFactory {

    private final BotProperties botProperties;
    private final BotTextService botTextService;

    public InlineKeyboardMarkup tagFilter(Set<String> tags) {
        BotProperties.TagKeyboard cfg = botProperties.tagKeyboard();

        if (tags.size() > cfg.maxButtonsBeforeInput()) {
            return new InlineKeyboardMarkup(new InlineKeyboardButton[][] {
                {
                    new InlineKeyboardButton(botTextService.get("bot.list.tag-filter-button"))
                            .callbackData(cfg.callbackInput())
                }
            });
        }

        List<String> tagList = new ArrayList<>(tags);
        boolean hasMore = tagList.size() > cfg.maxVisibleTags();
        List<String> visible = tagList.subList(0, hasMore ? cfg.maxVisibleTags() : tagList.size());

        List<InlineKeyboardButton[]> rows = buildRows(visible, cfg);
        if (hasMore) {
            rows.add(new InlineKeyboardButton[] {
                new InlineKeyboardButton(botTextService.get("bot.list.tag-more-button"))
                        .callbackData(cfg.callbackShowAll())
            });
        }
        return new InlineKeyboardMarkup(rows.toArray(new InlineKeyboardButton[0][]));
    }

    public InlineKeyboardMarkup tagFilterAll(Set<String> tags) {
        BotProperties.TagKeyboard cfg = botProperties.tagKeyboard();
        return new InlineKeyboardMarkup(buildRows(new ArrayList<>(tags), cfg).toArray(new InlineKeyboardButton[0][]));
    }

    private List<InlineKeyboardButton[]> buildRows(List<String> tags, BotProperties.TagKeyboard cfg) {
        List<InlineKeyboardButton[]> rows = new ArrayList<>();
        for (int i = 0; i < tags.size(); i += cfg.tagsPerRow()) {
            List<String> chunk = tags.subList(i, Math.min(i + cfg.tagsPerRow(), tags.size()));
            rows.add(chunk.stream()
                    .map(t -> new InlineKeyboardButton(t).callbackData(cfg.callbackPrefix() + t))
                    .toArray(InlineKeyboardButton[]::new));
        }
        return rows;
    }
}
