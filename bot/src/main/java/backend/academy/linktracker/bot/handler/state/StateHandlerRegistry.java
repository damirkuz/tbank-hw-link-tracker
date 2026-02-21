package backend.academy.linktracker.bot.handler.state;

import backend.academy.linktracker.bot.model.UserState;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class StateHandlerRegistry {

    private final Map<UserState, StateHandler> map;

    public StateHandlerRegistry(List<StateHandler> handlers) {
        Map<UserState, StateHandler> tempMap = new EnumMap<>(UserState.class);

        for (StateHandler handler : handlers) {
            tempMap.put(handler.getHandledState(), handler);
        }

        this.map = Collections.unmodifiableMap(tempMap);
    }

    public StateHandler getHandler(UserState state) {
        return map.get(state);
    }
}
