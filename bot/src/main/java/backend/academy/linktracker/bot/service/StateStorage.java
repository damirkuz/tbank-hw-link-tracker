package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.model.UserChatKey;
import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StateStorage {

    private final Map<UserChatKey, UserSession> sessionStore = new ConcurrentHashMap<>();

    private void addUserSessionIfAbsent(UserChatKey key) {
        sessionStore.computeIfAbsent(key, id -> new UserSession(UserState.IDLE));
    }

    public UserSession getUserSession(long userId, long chatId) {
        return getUserSession(keyOf(userId, chatId));
    }

    public UserSession getUserSession(UserChatKey key) {
        addUserSessionIfAbsent(key);
        return sessionStore.get(key).copy();
    }

    public void save(long userId, long chatId, UserSession session) {
        save(keyOf(userId, chatId), session);
    }

    public void save(UserChatKey key, UserSession session) {
        sessionStore.put(key, session.copy());
    }

    public void updateState(long userId, long chatId, UserState userState) {
        updateState(keyOf(userId, chatId), userState);
    }

    public void updateState(UserChatKey key, UserState userState) {
        UserSession userSession = getUserSession(key);
        userSession.setState(userState);
        save(key, userSession);
    }

    public void clearState(long userId, long chatId) {
        clearState(keyOf(userId, chatId));
    }

    public void clearState(UserChatKey key) {
        UserSession userSession = getUserSession(key);
        userSession.setState(UserState.IDLE);
        userSession.setTrackLink(null);

        save(key, userSession);
    }

    public UserChatKey keyOf(long userId, long chatId) {
        return new UserChatKey(userId, chatId);
    }
}
