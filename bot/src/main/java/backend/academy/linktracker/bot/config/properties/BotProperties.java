package backend.academy.linktracker.bot.config.properties;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "bot")
public record BotProperties(
        Map<String, @Valid CommandMessage> messages, @Valid TagKeyboard tagKeyboard) {
    public boolean isAliasForCommand(String command, String text) {
        return messages().values().stream()
                .filter(msg -> msg.command().equals(command))
                .flatMap(msg -> msg.aliases().stream())
                .anyMatch(alias -> alias.equals(text));
    }

    public String firstAliasOrCommand(String command) {
        return messages().values().stream()
                .filter(msg -> msg.command().equals(command))
                .flatMap(msg -> msg.aliases().stream())
                .findFirst()
                .orElse(command);
    }

    public String commandOf(String key) {
        return Optional.ofNullable(messages().get(key))
                .map(CommandMessage::command)
                .orElseThrow(() -> new IllegalStateException("Unknown command key: " + key));
    }

    public record TagKeyboard(
            int maxButtonsBeforeInput,
            int maxVisibleTags,
            int tagsPerRow,
            String callbackPrefix,
            String callbackShowAll,
            String callbackInput) {}
}
