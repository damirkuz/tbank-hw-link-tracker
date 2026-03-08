package backend.academy.linktracker.scrapper.client.tracked.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackoverflowQuestionResponse(
    @JsonProperty("question_id")
    Long questionId,

    String title,

    String link,

    @JsonProperty("last_activity_date")
    Long lastActivityDate,

    @JsonProperty("last_edit_date")
    Long lastEditDate
) {
}
