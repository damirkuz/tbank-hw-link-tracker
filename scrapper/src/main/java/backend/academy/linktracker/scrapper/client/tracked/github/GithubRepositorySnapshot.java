package backend.academy.linktracker.scrapper.client.tracked.github;

import java.time.Instant;

public record GithubRepositorySnapshot(String fullName, String defaultBranch, Instant pushedAt, Instant updatedAt) {}
