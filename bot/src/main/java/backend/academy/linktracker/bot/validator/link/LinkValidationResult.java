package backend.academy.linktracker.bot.validator.link;

public record LinkValidationResult(
    boolean valid,
    String code,
    String message
) {
    public static LinkValidationResult ok() {
        return new LinkValidationResult(true, null, null);
    }

    public static LinkValidationResult error(String code, String message) {
        return new LinkValidationResult(false, code, message);
    }
}
