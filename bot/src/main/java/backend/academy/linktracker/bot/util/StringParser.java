package backend.academy.linktracker.bot.util;

public class StringParser {

    // парсит тег из "/list дом"
    public static String parseAfterSpace(String command) {
        String[] parts = command.trim().split("\\s+", 2);

        if (parts.length < 2 || parts[1].isBlank()) {
            return "";
        }

        return parts[1].trim();
    }
}
