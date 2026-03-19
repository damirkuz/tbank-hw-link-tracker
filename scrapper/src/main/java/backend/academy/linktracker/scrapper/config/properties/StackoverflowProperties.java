package backend.academy.linktracker.scrapper.config.properties;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.stackoverflow")
@Validated
public record StackoverflowProperties(
        @NotEmpty String baseUrl, @NotEmpty String key, String accessToken) {}
