package backend.academy.linktracker.bot.properties.message;

import jakarta.validation.constraints.NotEmpty;

public record OtherMessage(@NotEmpty String answer) {}
