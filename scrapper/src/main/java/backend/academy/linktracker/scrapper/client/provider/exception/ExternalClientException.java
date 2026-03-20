package backend.academy.linktracker.scrapper.client.provider.exception;

import java.io.Serial;
import java.net.URI;

public class ExternalClientException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String client;
    private final String operation;
    private final URI link;

    public ExternalClientException(String client, String operation, URI link, String message) {
        super(message);
        this.client = client;
        this.operation = operation;
        this.link = link;
    }

    public ExternalClientException(String client, String operation, URI link, String message, Throwable cause) {
        super(message, cause);
        this.client = client;
        this.operation = operation;
        this.link = link;
    }

    public String client() {
        return client;
    }

    public String operation() {
        return operation;
    }

    public URI link() {
        return link;
    }
}
