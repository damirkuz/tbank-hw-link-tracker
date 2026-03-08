package backend.academy.linktracker.bot.validator.link;

import java.net.URI;

public interface LinkValidator {
    boolean supports(URI uri);

    LinkValidationResult validate(URI uri);
}
