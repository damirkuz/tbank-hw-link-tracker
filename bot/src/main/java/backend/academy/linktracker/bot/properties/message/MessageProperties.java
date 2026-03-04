package backend.academy.linktracker.bot.properties.message;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "bot.messages")
public record MessageProperties(
        @Valid CommandMessage startCommand,

        @Valid CommandMessage helpCommand,

        @Valid OtherMessage unknownUpdate) {}
