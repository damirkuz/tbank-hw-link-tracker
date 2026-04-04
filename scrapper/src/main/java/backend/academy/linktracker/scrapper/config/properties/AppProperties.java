package backend.academy.linktracker.scrapper.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(@NotNull AccessType accessType) {

    public enum AccessType {
        SQL,
        ORM,
        MEMORY
    }
}
