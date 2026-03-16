package backend.academy.linktracker.bot.config.properties;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "bot")
public record BotProperties(Map<String, @Valid CommandMessage> messages) {}
