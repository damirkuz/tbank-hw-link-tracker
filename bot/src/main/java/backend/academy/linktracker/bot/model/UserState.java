package backend.academy.linktracker.bot.model;

import lombok.Getter;

@Getter
public enum UserState {
    IDLE(InterruptionPolicy.ALLOW_ALL),
    TRACK_WAIT_LINK(InterruptionPolicy.ALLOW_CANCEL_ONLY),
    TRACK_WAIT_TAGS(InterruptionPolicy.ALLOW_CANCEL_ONLY),
    LIST_WAIT_TAG(InterruptionPolicy.ALLOW_CANCEL_ONLY),
    UNTRACK_WAIT_LINK(InterruptionPolicy.ALLOW_CANCEL_ONLY);

    private final InterruptionPolicy interruptionPolicy;

    UserState(InterruptionPolicy interruptionPolicy) {
        this.interruptionPolicy = interruptionPolicy;
    }
}
