package backend.academy.linktracker.bot.properties;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scrapper")
public record ScrapperProperties(@Valid String baseUrl) {}
