package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "bot.messages")
public record MessageProperties(
        @NotEmpty String startCommand,

        @NotEmpty String helpCommand,

        @NotEmpty String unknownUpdate) {}
