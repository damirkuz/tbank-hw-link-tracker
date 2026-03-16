package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StateStorage {

    private final Map<Long, UserSession> sessionStore;

    private void addUserSessionIfAbsent(long userId) {
        sessionStore.computeIfAbsent(userId, id -> new UserSession(UserState.IDLE));
    }

    public UserSession getUserSession(long userId) {
        addUserSessionIfAbsent(userId);

        return sessionStore.get(userId);
    }
}
