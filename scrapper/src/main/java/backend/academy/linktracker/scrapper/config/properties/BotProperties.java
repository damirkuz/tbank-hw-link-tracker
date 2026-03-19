package backend.academy.linktracker.scrapper.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "bot")
@Validated
public record BotProperties(
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
