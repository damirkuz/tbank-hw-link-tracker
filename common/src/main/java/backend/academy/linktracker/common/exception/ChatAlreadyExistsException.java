package backend.academy.linktracker.common.exception;

public class ChatAlreadyExistsException extends RuntimeException {
    public ChatAlreadyExistsException(String message) {
        super(message);
    }

    public ChatAlreadyExistsException() {
        super();
    }
}
