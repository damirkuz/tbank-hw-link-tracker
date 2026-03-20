package backend.academy.linktracker.contracts.exception;

import java.io.Serial;

public class LinkAlreadyTrackedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LinkAlreadyTrackedException(String message) {
        super(message);
    }

    public LinkAlreadyTrackedException() {
        super();
    }
}
