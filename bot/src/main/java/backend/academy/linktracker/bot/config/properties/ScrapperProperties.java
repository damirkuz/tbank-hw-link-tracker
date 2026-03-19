package backend.academy.linktracker.bot.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "scrapper")
@Validated
public record ScrapperProperties(
        @NotNull Transport transport,
        @Valid Http http,
        @Valid Grpc grpc) {
    public enum Transport {
        HTTP,
        GRPC
    }

    public record Http(@NotBlank String baseUrl) {}

    public record Grpc(@NotBlank String host, int port) {}
}
