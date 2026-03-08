package backend.academy.linktracker.bot.util;

import org.springframework.stereotype.Component;

@Component
public class StringParser {

    // парсит команды "/start@name_bot тест"
    public String parseCommand(String command) {

        String beforeSpace = command.trim().split("\\s+", 2)[0];
        return beforeSpace.split("@", 2)[0];
    }

    // парсит тег из "/list дом"
    public String parseAfterSpace(String command) {
        String[] parts = command.trim().split("\\s+", 2);

        if (parts.length < 2 || parts[1].isBlank()) {
            return "";
        }

        return parts[1].trim();
    }

}
