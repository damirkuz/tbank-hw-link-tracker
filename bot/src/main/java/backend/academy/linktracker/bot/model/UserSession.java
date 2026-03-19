package backend.academy.linktracker.bot.model;

import java.net.URI;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserSession {

    private UserState state;

    private URI trackLink;

    public UserSession(UserState state) {
        this.state = state;
    }
}
