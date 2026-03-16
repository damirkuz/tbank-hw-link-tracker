package backend.academy.linktracker.bot.config;

import backend.academy.linktracker.bot.model.UserSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionConfiguration {

    @Bean
    public Map<Long, UserSession> sessionStore() {
        return new ConcurrentHashMap<>();
    }
}
