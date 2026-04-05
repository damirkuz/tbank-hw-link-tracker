package backend.academy.linktracker.bot.config.properties;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CommandMessage(
        @NotEmpty String command, @NotEmpty String description, List<String> aliases) {
    public CommandMessage {
        aliases = aliases != null ? aliases : List.of();
    }
}
