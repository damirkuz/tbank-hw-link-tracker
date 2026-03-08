package backend.academy.linktracker.scrapper.client.tracked.stackoverflow;

import java.time.Instant;

public record StackoverflowQuestionSnapshot(
        Long questionId, String title, String link, Instant lastActivityDate, Instant lastEditDate) {}
