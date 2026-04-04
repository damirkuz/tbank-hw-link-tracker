package backend.academy.linktracker.bot.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "scrapper")
@Validated
public record ScrapperProperties(@NotNull Transport transport, String httpUrl, String grpcHost, Integer grpcPort) {
    public enum Transport {
        HTTP,
        GRPC
    }
}
