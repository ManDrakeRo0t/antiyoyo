package ru.bogatov.antiyoyo.server.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class GameFinishedEvent extends ApplicationEvent {

    private final UUID gameSessionId;

    public GameFinishedEvent(Object source, UUID gameSessionId) {
        super(source);
        this.gameSessionId = gameSessionId;
    }

}
