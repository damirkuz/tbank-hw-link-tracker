package backend.academy.linktracker.bot.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserSession {

    private UserState state;

    private String trackLink;

    public UserSession(UserState state) {
        this.state = state;
    }
}
