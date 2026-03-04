package backend.academy.linktracker.bot.util;

import org.springframework.stereotype.Component;

@Component
public class StringParser {

    // парсит команды "/start@name_bot тест"
    public String parseCommand(String command) {

        String beforeSpace = command.trim().split("\\s+", 2)[0];
        return beforeSpace.split("@", 2)[0];
    }
}
