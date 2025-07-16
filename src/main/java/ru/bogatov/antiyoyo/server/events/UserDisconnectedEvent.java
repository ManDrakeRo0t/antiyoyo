package ru.bogatov.antiyoyo.server.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserDisconnectedEvent extends ApplicationEvent {

    private final String userId;
    private final String gameSessionId;

    public UserDisconnectedEvent(Object source, String userId, String gameSessionId) {
        super(source);
        this.userId = userId;
        this.gameSessionId = gameSessionId;
    }
}
