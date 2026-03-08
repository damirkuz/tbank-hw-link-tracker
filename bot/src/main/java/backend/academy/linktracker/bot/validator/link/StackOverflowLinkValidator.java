package backend.academy.linktracker.bot.validator.link;

import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowLinkValidator implements LinkValidator {

    @Override
    public boolean supports(URI uri) {
        return "stackoverflow.com".equalsIgnoreCase(uri.getHost())
                || "www.stackoverflow.com".equalsIgnoreCase(uri.getHost());
    }

    @Override
    public LinkValidationResult validate(URI uri) {
        String[] parts = uri.getPath().split("/");
        if (parts.length < 3 || !"questions".equals(parts[1]) || !parts[2].matches("\\d+")) {
            return LinkValidationResult.error(
                    "INVALID_STACKOVERFLOW_URL", "Ожидается ссылка вида https://stackoverflow.com/questions/{id}/...");
        }
        return LinkValidationResult.ok();
    }
}
