package backend.academy.linktracker.scrapper.util;

import backend.academy.linktracker.scrapper.client.tracked.github.RepoInfo;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class StringParser {

    public RepoInfo parseGithubRepositoryLink(String repositoryLink) {
        URI uri = URI.create(repositoryLink);
        String host = uri.getHost();

        if (host == null || (!host.equals("github.com") && !host.equals("www.github.com"))) {
            throw new IllegalArgumentException("Not a GitHub repository link: " + repositoryLink);
        }

        String[] parts = uri.getPath().split("/");

        if (parts.length < 3 || parts[1].isBlank() || parts[2].isBlank()) {
            throw new IllegalArgumentException("Invalid GitHub repository link: " + repositoryLink);
        }

        String owner = parts[1];
        String repo = parts[2];

        if (repo.endsWith(".git")) {
            repo = repo.substring(0, repo.length() - 4);
        }

        return new RepoInfo(owner, repo);
    }

    public long parseStackoverflowQuestionId(String questionLink) {
        URI uri = URI.create(questionLink);
        String host = uri.getHost();

        if (host == null || (!host.equals("stackoverflow.com") && !host.equals("ru.stackoverflow.com"))) {
            throw new IllegalArgumentException("Not a Stack Overflow question link: " + questionLink);
        }

        String[] parts = uri.getPath().split("/");

        if (parts.length < 3 || !"questions".equals(parts[1])) {
            throw new IllegalArgumentException("Invalid Stack Overflow question link: " + questionLink);
        }

        try {
            return Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid question id in link: " + questionLink, e);
        }
    }
}
