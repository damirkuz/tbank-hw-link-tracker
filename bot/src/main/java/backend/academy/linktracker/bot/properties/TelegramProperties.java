package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.telegram")
public record TelegramProperties(
        @NotEmpty @URL String url, @NotEmpty String token, Duration updateListenerSleep, boolean debug) {}
