package backend.academy.linktracker.bot.util;

import org.springframework.stereotype.Component;

@Component
public class StringParser {

    public String parseCommand(String command) {
        return command.trim().split("\\s+", 2)[0];
    }
}
