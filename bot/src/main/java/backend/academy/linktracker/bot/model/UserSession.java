package backend.academy.linktracker.bot.model;

import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class UserSession {

    private UserState state;

    private URI trackLink;

    public UserSession(UserState state) {
        this.state = state;
    }

    public UserSession copy() {
        return new UserSession(this.state, this.trackLink);
    }
}
