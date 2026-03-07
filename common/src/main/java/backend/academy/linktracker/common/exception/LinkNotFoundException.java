package backend.academy.linktracker.common.exception;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String message) {
        super(message);
    }

    public LinkNotFoundException() {
        super();
    }
}
