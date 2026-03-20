package backend.academy.linktracker.scrapper.client.provider.github;

import backend.academy.linktracker.scrapper.client.provider.AbstractRestTrackedClient;
import backend.academy.linktracker.scrapper.client.provider.BaseTrackedClient;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.util.StringParser;
import java.net.URI;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GithubTrackedClient extends AbstractRestTrackedClient implements BaseTrackedClient {

    private final RestClient githubRestClient;

    @Override
    public Instant getLastUpdate(URI link) {
        return getRepositorySnapshot(link).pushedAt();
    }

    @Override
    public TrackedResource getTrackedResource() {
        return TrackedResource.GITHUB;
    }

    public GithubRepositorySnapshot getRepositorySnapshot(URI repositoryLink) {
        RepoInfo repoInfo = StringParser.parseGithubRepositoryLink(repositoryLink);

        GithubRepositoryResponse response = getBody(
                githubRestClient.get().uri("/repos/{owner}/{repo}", repoInfo.owner(), repoInfo.repo()),
                GithubRepositoryResponse.class,
                repositoryLink,
                "github",
                "getRepositorySnapshot");

        return new GithubRepositorySnapshot(
                response.fullName(), response.defaultBranch(), response.pushedAt(), response.updatedAt());
    }
}
