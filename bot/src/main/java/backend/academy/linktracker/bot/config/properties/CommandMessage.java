package backend.academy.linktracker.bot.config.properties;

import jakarta.validation.constraints.NotEmpty;

public record CommandMessage(
        @NotEmpty String command, @NotEmpty String description) {}
