package backend.academy.linktracker.scrapper.client.provider.github;

import java.time.Instant;

public record GithubRepositorySnapshot(String fullName, String defaultBranch, Instant pushedAt, Instant updatedAt) {}
