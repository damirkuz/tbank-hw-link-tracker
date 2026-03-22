package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StateStorage {

    private final Map<Long, UserSession> sessionStore = new ConcurrentHashMap<>();

    private void addUserSessionIfAbsent(long userId) {
        sessionStore.computeIfAbsent(userId, id -> new UserSession(UserState.IDLE));
    }

    public UserSession getUserSession(long userId) {
        addUserSessionIfAbsent(userId);
        return sessionStore.get(userId).copy();
    }

    public void save(long userId, UserSession session) {
        sessionStore.put(userId, session.copy());
    }

    public void updateState(long userId, UserState userState) {
        UserSession userSession = getUserSession(userId);
        userSession.setState(userState);
        save(userId, userSession);
    }

    public void clearState(long userId) {
        UserSession userSession = getUserSession(userId);
        userSession.setState(UserState.IDLE);
        userSession.setTrackLink(null);

        save(userId, userSession);
    }
}
