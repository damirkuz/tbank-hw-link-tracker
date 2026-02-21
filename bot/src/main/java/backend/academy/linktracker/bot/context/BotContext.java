package backend.academy.linktracker.bot.context;

import backend.academy.linktracker.bot.properties.MessageProperties;
import backend.academy.linktracker.bot.repository.StateRepository;
import com.pengrad.telegrambot.TelegramBot;
import org.springframework.stereotype.Component;

@Component
public record BotContext(
    TelegramBot bot,
    StateRepository stateRepository,
    MessageProperties messageProperties
) {
}
