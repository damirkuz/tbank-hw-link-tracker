package backend.academy.linktracker.scrapper.client.provider.stackoverflow;

import backend.academy.linktracker.scrapper.client.provider.BaseTrackedClient;
import backend.academy.linktracker.scrapper.config.properties.StackoverflowProperties;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.util.StringParser;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Component
public class StackoverflowTrackedClient implements BaseTrackedClient {

    private final RestClient restClient;
    private final StackoverflowProperties properties;
    private final StringParser stringParser;

    public StackoverflowTrackedClient(
            RestClient.Builder restClientBuilder, StackoverflowProperties properties, StringParser stringParser) {
        this.properties = properties;
        this.stringParser = stringParser;
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
    }

    @Override
    public Instant getLastUpdate(URI link) {
        return getQuestionSnapshot(link).lastActivityDate();
    }

    @Override
    public TrackedResource getTrackedResource() {
        return TrackedResource.STACKOVERFLOW;
    }

    public StackoverflowQuestionSnapshot getQuestionSnapshot(URI questionLink) {
        long questionId = stringParser.parseStackoverflowQuestionId(questionLink);

        StackoverflowResponse response = restClient
                .get()
                .uri(uriBuilder -> buildQuestionUri(uriBuilder, questionId))
                .retrieve()
                .body(StackoverflowResponse.class);

        if (response == null || response.items() == null || response.items().isEmpty()) {
            throw new IllegalStateException("StackOverflow response is empty");
        }

        StackoverflowQuestionResponse question = response.items().getFirst();

        return new StackoverflowQuestionSnapshot(
                question.questionId(),
                question.title(),
                question.link(),
                toInstant(question.lastActivityDate()),
                toInstant(question.lastEditDate()));
    }

    private URI buildQuestionUri(UriBuilder uriBuilder, long questionId) {
        UriBuilder builder = uriBuilder
                .path("/questions/{id}")
                .queryParam("site", "stackoverflow")
                .queryParam("key", properties.key());

        if (properties.accessToken() != null && !properties.accessToken().isBlank()) {
            builder.queryParam("access_token", properties.accessToken());
        }

        return builder.build(questionId);
    }

    private Instant toInstant(Long epochSeconds) {
        return epochSeconds == null ? null : Instant.ofEpochSecond(epochSeconds);
    }

    private record StackoverflowResponse(List<StackoverflowQuestionResponse> items) {}
}
