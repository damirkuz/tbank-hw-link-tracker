package backend.academy.linktracker.common.exception;

public class LinkAlreadyTrackedException extends RuntimeException {
    public LinkAlreadyTrackedException(String message) {
        super(message);
    }

    public LinkAlreadyTrackedException() {
        super();
    }
}
