package ru.bogatov.antiyoyo.server.domain;

import lombok.Data;
import ru.bogatov.antiyoyo.game.model.Hex;
import ru.bogatov.antiyoyo.game.model.Move;
import ru.bogatov.antiyoyo.game.model.entity.EntityType;

@Data
public class GameEvent {

    EventType type;
    Move move;
    Hex hex;
    EntityType entityType;

}
