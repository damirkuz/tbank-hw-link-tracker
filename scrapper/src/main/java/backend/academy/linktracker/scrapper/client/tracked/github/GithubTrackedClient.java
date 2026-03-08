package backend.academy.linktracker.scrapper.client.tracked.github;

import backend.academy.linktracker.scrapper.client.tracked.BaseTrackedClient;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import backend.academy.linktracker.scrapper.util.StringParser;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GithubTrackedClient implements BaseTrackedClient {

    private final RestClient restClient;
    private final StringParser stringParser;

    public GithubTrackedClient(
        RestClient.Builder restClientBuilder,
        GithubProperties properties, StringParser stringParser
    ) {
        this.stringParser = stringParser;
        this.restClient = restClientBuilder
            .baseUrl(properties.getBaseUrl())
            .defaultHeader("Authorization", "Bearer " + properties.getToken())
            .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
            .defaultHeader("Accept", "application/vnd.github+json")
            .build();
    }

    @Override
    public Instant getLastUpdate(String link) {
        return getRepositorySnapshot(link).pushedAt();
    }

    @Override
    public TrackedResource getTrackedResource() {
        return TrackedResource.GITHUB;
    }


    public GithubRepositorySnapshot getRepositorySnapshot(String repositoryLink) {
        RepoInfo repoInfo = stringParser.parseGithubRepositoryLink(repositoryLink);

        GithubRepositoryResponse response = restClient
            .get()
            .uri("/repos/{owner}/{repo}", repoInfo.owner(), repoInfo.repo())
            .retrieve()
            .body(GithubRepositoryResponse.class);

        if (response == null) {
            throw new IllegalStateException("GitHub response is empty");
        }

        return new GithubRepositorySnapshot(
            response.fullName(),
            response.defaultBranch(),
            response.pushedAt(),
            response.updatedAt()
        );
    }
}
