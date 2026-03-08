package backend.academy.linktracker.bot.validator.link;

import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class GitHubLinkValidator implements LinkValidator {

    @Override
    public boolean supports(URI uri) {
        return "github.com".equalsIgnoreCase(uri.getHost()) || "www.github.com".equalsIgnoreCase(uri.getHost());
    }

    @Override
    public LinkValidationResult validate(URI uri) {
        String[] parts = uri.getPath().split("/");
        if (parts.length != 3 || parts[1].isBlank() || parts[2].isBlank()) {
            return LinkValidationResult.error(
                    "INVALID_GITHUB_REPO_URL", "Ожидается ссылка вида https://github.com/{owner}/{repo}");
        }
        return LinkValidationResult.ok();
    }
}
