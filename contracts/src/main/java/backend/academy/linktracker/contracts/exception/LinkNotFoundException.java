package backend.academy.linktracker.contracts.exception;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String message) {
        super(message);
    }

    public LinkNotFoundException() {
        super();
    }
}
