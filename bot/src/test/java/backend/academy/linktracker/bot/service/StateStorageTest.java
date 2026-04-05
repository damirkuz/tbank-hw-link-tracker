package backend.academy.linktracker.bot.service;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.bot.model.UserSession;
import backend.academy.linktracker.bot.model.UserState;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StateStorage")
class StateStorageTest {

    private final StateStorage stateStorage = new StateStorage();

    @Test
    @DisplayName("Хранит независимые сессии для одного пользователя в разных чатах")
    void shouldStoreSessionsPerUserAndChat() {
        UserSession firstChatSession = stateStorage.getUserSession(7L, 100L);
        firstChatSession.setState(UserState.TRACK_WAIT_LINK);
        stateStorage.save(7L, 100L, firstChatSession);

        UserSession secondChatSession = stateStorage.getUserSession(7L, 200L);

        assertThat(secondChatSession.getState()).isEqualTo(UserState.IDLE);
        assertThat(stateStorage.getUserSession(7L, 100L).getState()).isEqualTo(UserState.TRACK_WAIT_LINK);
    }

    @Test
    @DisplayName("clearState очищает только выбранный чат")
    void shouldClearOnlySelectedChatState() {
        UserSession firstChatSession = new UserSession(UserState.TRACK_WAIT_TAGS, URI.create("https://github.com/a/b"));
        UserSession secondChatSession =
                new UserSession(UserState.UNTRACK_WAIT_LINK, URI.create("https://github.com/c/d"));

        stateStorage.save(7L, 100L, firstChatSession);
        stateStorage.save(7L, 200L, secondChatSession);

        stateStorage.clearState(7L, 100L);

        UserSession clearedSession = stateStorage.getUserSession(7L, 100L);
        UserSession untouchedSession = stateStorage.getUserSession(7L, 200L);

        assertThat(clearedSession.getState()).isEqualTo(UserState.IDLE);
        assertThat(clearedSession.getTrackLink()).isNull();
        assertThat(untouchedSession.getState()).isEqualTo(UserState.UNTRACK_WAIT_LINK);
        assertThat(untouchedSession.getTrackLink()).isEqualTo(URI.create("https://github.com/c/d"));
    }

    @Test
    @DisplayName("getUserSession возвращает копию и защищает хранимое состояние от внешней мутации")
    void shouldReturnCopies() {
        UserSession session = stateStorage.getUserSession(7L, 100L);
        session.setState(UserState.TRACK_WAIT_LINK);
        stateStorage.save(7L, 100L, session);

        UserSession loadedSession = stateStorage.getUserSession(7L, 100L);
        loadedSession.setState(UserState.IDLE);
        loadedSession.setTrackLink(URI.create("https://example.com"));

        UserSession persistedSession = stateStorage.getUserSession(7L, 100L);
        assertThat(persistedSession.getState()).isEqualTo(UserState.TRACK_WAIT_LINK);
        assertThat(persistedSession.getTrackLink()).isNull();
    }
}
