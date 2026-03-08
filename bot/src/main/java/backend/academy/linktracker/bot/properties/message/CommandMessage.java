package backend.academy.linktracker.bot.properties.message;

import jakarta.validation.constraints.NotEmpty;

public record CommandMessage(
        @NotEmpty String command, @NotEmpty String description) {}
