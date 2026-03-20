package backend.academy.linktracker.contracts.exception;

import java.io.Serial;

public class LinkNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LinkNotFoundException(String message) {
        super(message);
    }

    public LinkNotFoundException() {
        super();
    }
}
