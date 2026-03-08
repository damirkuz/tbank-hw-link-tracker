package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
public record BotProperties(
    @Valid String baseUrl
) {}
