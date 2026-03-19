package backend.academy.linktracker.scrapper.config.properties;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.github")
@Validated
public record GithubProperties(
        @NotEmpty String baseUrl, @NotEmpty String token) {}
