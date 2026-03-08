package backend.academy.linktracker.bot.validator.link;

import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkValidationService {

    private final List<LinkValidator> validators;

    public LinkValidationResult validate(String rawLink) {
        final URI uri;

        try {
            uri = URI.create(rawLink.trim());
        } catch (IllegalArgumentException e) {
            return LinkValidationResult.error("INVALID_URL", "Некорректный URL");
        }

        return validators.stream()
                .filter(v -> v.supports(uri))
                .findFirst()
                .map(v -> v.validate(uri))
                .orElse(LinkValidationResult.error("UNSUPPORTED_HOST", "Поддерживаются только GitHub и StackOverflow"));
    }
}
