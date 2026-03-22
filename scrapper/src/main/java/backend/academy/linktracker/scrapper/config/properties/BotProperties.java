package backend.academy.linktracker.scrapper.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "bot")
@Validated
public record BotProperties(@NotNull Transport transport) {
    public enum Transport {
        HTTP,
        GRPC
    }
}
