package backend.academy.linktracker.scrapper.client.provider.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record GithubRepositoryResponse(
        @JsonProperty("full_name") String fullName,

        @JsonProperty("default_branch") String defaultBranch,

        @JsonProperty("pushed_at") Instant pushedAt,

        @JsonProperty("updated_at") Instant updatedAt) {}
