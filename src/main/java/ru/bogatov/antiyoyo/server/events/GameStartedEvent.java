package ru.bogatov.antiyoyo.server.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.model.common.Color;

import java.util.List;
import java.util.UUID;

@Getter
public class GameStartedEvent extends ApplicationEvent {

    private final UUID gameSessionId;
    private final List<Pair<UUID, Color>> usersId;

    public GameStartedEvent(Object source, UUID gameSessionId, List<Pair<UUID, Color>> usersId) {
        super(source);
        this.gameSessionId = gameSessionId;
        this.usersId = usersId;
    }

}

