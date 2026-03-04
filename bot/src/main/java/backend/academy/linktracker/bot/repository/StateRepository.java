package backend.academy.linktracker.bot.repository;

import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StateRepository {

    private final Map<Long, UserSession> sessionStore;

    private void addUserSessionIfAbsent(long userId) {
        sessionStore.computeIfAbsent(userId, id -> new UserSession(UserState.IDLE));
    }

    public void setUserState(long userId, UserState state) {
        addUserSessionIfAbsent(userId);

        sessionStore.get(userId).setState(state);
    }

    public UserState getUserState(long userId) {
        addUserSessionIfAbsent(userId);

        return sessionStore.get(userId).getState();
    }
}
