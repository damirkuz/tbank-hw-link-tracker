package backend.academy.linktracker.e2e;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

class BotUpdatesIT extends AbstractHttpE2EIT {

    @Test
    void validUpdateRequestReturns200() {
        String body = """
            {
              "id": 1,
              "url": "https://github.com/user/repo",
              "description": "Repository updated",
              "tgChatIds": [1, 2]
            }
            """;

        given().baseUri(botUrl())
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/updates")
                .then()
                .statusCode(200);
    }

    @Test
    void invalidUpdateRequestReturns4xx() {
        String body = """
            {
              "id": "wrong-type",
              "description": 123
            }
            """;

        given().baseUri(botUrl())
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/updates")
                .then()
                .statusCode(org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.is(400), org.hamcrest.Matchers.is(422)));
    }
}
