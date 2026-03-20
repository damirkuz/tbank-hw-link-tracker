package backend.academy.linktracker.contracts.exception;

import java.io.Serial;

public class ChatAlreadyExistsException extends RuntimeException {

    // линтер требует в кастомных исключениях писать serialVersionUID
    @Serial
    private static final long serialVersionUID = 1L;

    public ChatAlreadyExistsException(String message) {
        super(message);
    }

    public ChatAlreadyExistsException() {
        super();
    }
}
