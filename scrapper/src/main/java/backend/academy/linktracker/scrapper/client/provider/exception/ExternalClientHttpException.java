package backend.academy.linktracker.scrapper.client.provider.exception;

import java.io.Serial;
import java.net.URI;
import org.springframework.http.HttpStatusCode;

public class ExternalClientHttpException extends ExternalClientException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final HttpStatusCode statusCode;
    private final String responseBody;

    public ExternalClientHttpException(
            String client, String operation, URI link, HttpStatusCode statusCode, String responseBody) {
        super(client, operation, link, "HTTP error during external call: status=" + statusCode.value());
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public HttpStatusCode statusCode() {
        return statusCode;
    }

    public String responseBody() {
        return responseBody;
    }
}
