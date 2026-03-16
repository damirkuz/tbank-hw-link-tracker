package backend.academy.linktracker.contracts.exception;

public class LinkAlreadyTrackedException extends RuntimeException {
    public LinkAlreadyTrackedException(String message) {
        super(message);
    }

    public LinkAlreadyTrackedException() {
        super();
    }
}
