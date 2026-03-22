package backend.academy.linktracker.bot.update.context;

import java.util.Objects;
import java.util.Optional;

public record UpdateContext(Integer updateId, Long chatId, Long userId, String messageText, String callbackData) {
    public boolean hasMessageText() {
        return messageText != null && !messageText.isBlank();
    }

    public boolean hasCallbackData() {
        return callbackData != null && !callbackData.isBlank();
    }

    public boolean isCommand(String command) {
        return hasMessageText() && Objects.equals(messageText, command);
    }

    public boolean isCallback(String callback) {
        return hasCallbackData() && Objects.equals(callbackData, callback);
    }

    public boolean hasCommand() {
        return commandToken().isPresent();
    }

    public Optional<String> commandToken() {
        if (!hasMessageText()) {
            return Optional.empty();
        }

        String text = messageText.trim();
        if (!text.startsWith("/")) {
            return Optional.empty();
        }

        int end = findCommandEnd(text);
        return Optional.of(text.substring(0, end).toLowerCase());
    }

    public Optional<String> commandName() {
        return commandToken().map(this::extractCommandName);
    }

    public boolean isCommandName(String commandName) {
        if (commandName == null || commandName.isBlank()) {
            return false;
        }

        String normalized = normalizeCommandName(commandName);
        return commandName().map(normalized::equals).orElse(false);
    }

    public boolean isCommandForBot(String botUsername) {
        Optional<String> token = commandToken();
        if (token.isEmpty()) {
            return false;
        }

        String commandToken = token.get();
        int mentionIndex = commandToken.indexOf('@');
        if (mentionIndex < 0) {
            return true;
        }

        if (botUsername == null || botUsername.isBlank()) {
            return false;
        }

        String actualBotUsername = commandToken.substring(mentionIndex + 1);
        return actualBotUsername.equalsIgnoreCase(botUsername);
    }

    public Optional<String> commandIdentifierForBot(String botUsername) {
        if (!isCommandForBot(botUsername)) {
            return Optional.empty();
        }

        return commandToken().map(token -> {
            int mentionIndex = token.indexOf('@');
            return mentionIndex >= 0 ? token.substring(0, mentionIndex) : token;
        });
    }

    public boolean isCallbackOrCommandAction(String name) {
        return isCallback(name) || isCommandName(name);
    }

    private String extractCommandName(String commandToken) {
        String withoutSlash = commandToken.substring(1);
        int mentionIndex = withoutSlash.indexOf('@');
        return mentionIndex >= 0 ? withoutSlash.substring(0, mentionIndex) : withoutSlash;
    }

    private String normalizeCommandName(String commandName) {
        return commandName.startsWith("/") ? commandName.substring(1).toLowerCase() : commandName.toLowerCase();
    }

    private int findCommandEnd(String text) {
        int spaceIndex = text.indexOf(' ');
        return spaceIndex >= 0 ? spaceIndex : text.length();
    }
}
