package backend.academy.linktracker.scrapper.client.provider.stackoverflow;

import backend.academy.linktracker.scrapper.client.provider.AbstractRestTrackedClient;
import backend.academy.linktracker.scrapper.client.provider.BaseTrackedClient;
import backend.academy.linktracker.scrapper.config.properties.StackoverflowProperties;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.util.StringParser;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Component
@RequiredArgsConstructor
public class StackoverflowTrackedClient extends AbstractRestTrackedClient implements BaseTrackedClient {

    private final RestClient stackoverflowRestClient;
    private final StackoverflowProperties properties;

    @Override
    public Instant getLastUpdate(URI link) {
        return getQuestionSnapshot(link).lastActivityDate();
    }

    @Override
    public TrackedResource getTrackedResource() {
        return TrackedResource.STACKOVERFLOW;
    }

    public StackoverflowQuestionSnapshot getQuestionSnapshot(URI questionLink) {
        long questionId = StringParser.parseStackoverflowQuestionId(questionLink);

        StackoverflowResponse response = getBody(
                stackoverflowRestClient.get().uri(uriBuilder -> buildQuestionUri(uriBuilder, questionId)),
                StackoverflowResponse.class,
                questionLink,
                "stackoverflow",
                "getQuestionSnapshot");

        if (response.items() == null || response.items().isEmpty()) {
            throw emptyResponse(
                    "stackoverflow", "getQuestionSnapshot", questionLink, "StackOverflow response items are empty");
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
