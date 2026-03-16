package backend.academy.linktracker.contracts.exception;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException(String message) {
        super(message);
    }

    public ChatNotFoundException() {
        super();
    }
}
