package backend.academy.linktracker.contracts.exception;

public class ChatAlreadyExistsException extends RuntimeException {
    public ChatAlreadyExistsException(String message) {
        super(message);
    }

    public ChatAlreadyExistsException() {
        super();
    }
}
