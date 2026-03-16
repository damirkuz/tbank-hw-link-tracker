package backend.academy.linktracker.bot.config;

import backend.academy.linktracker.bot.config.properties.TelegramProperties;
import com.pengrad.telegrambot.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramConfiguration {

    @Bean
    public TelegramBot telegramBot(TelegramProperties properties) {
        var builder = new TelegramBot.Builder(properties.token())
                .apiUrl(properties.url())
                .updateListenerSleep(properties.updateListenerSleep().toMillis());
        if (properties.debug()) {
            builder.debug();
        }

        return builder.build();
    }
}
